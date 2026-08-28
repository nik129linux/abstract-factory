import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * El agente de verdad: habla con un modelo corriendo detras de Ollama.
 *
 * java.net.http viene con el JDK desde Java 11, asi que sigue sin haber
 * libreria. Ollama expone /api/generate y con "stream": false contesta un solo
 * JSON plano, que es justo lo que Json.value sabe leer.
 *
 * El formato de respuesta que se le pide son tres lineas con prefijo, no JSON.
 * Un modelo de 30B se equivoca escribiendo JSON -- una coma de mas y no queda
 * nada que parsear -- y en cambio "NOMBRE:" al principio de la linea lo cumple
 * siempre, y si sobra texto alrededor se ignora solo.
 */
public class OllamaAgent implements Agent {

    public static final String DEFAULT_MODEL = "gemma4:31b-cloud";
    private static final String DEFAULT_HOST = "http://localhost:11434";

    private final String model;
    private final URI endpoint;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private String lastPrompt = "";
    private String lastReply = "";

    public OllamaAgent(String model, String host) {
        this.model = model == null || model.isBlank() ? DEFAULT_MODEL : model;
        this.endpoint = URI.create((host == null || host.isBlank() ? DEFAULT_HOST : host)
                + "/api/generate");
    }

    @Override
    public String name() {
        return "ollama " + model;
    }

    @Override
    public String chooseTradition(Order order, List<String> available) {
        String prompt = """
                Sos el jefe de cocina de un servicio de catering.
                Llego este pedido: %s

                Cocinas disponibles: %s

                Elegi la que mejor le sirve al pedido.
                Responde SOLO con una de esas palabras, en minuscula, sin explicar nada.
                """.formatted(order.describe(), String.join(", ", available));
        return ask(prompt).trim();
    }

    @Override
    public void fill(Course course, Order order) {
        String prompt = """
                Sos el jefe de cocina de un servicio de catering de cocina %s.
                Pedido: %s

                Te toca esta parte del menu: %s.
                Reglas de la casa que no podes romper: %s
                Nunca uses: %s

                Responde exactamente en tres lineas, sin titulos ni comentarios:
                NOMBRE: <nombre del plato>
                INGREDIENTES: <ingrediente> | <ingrediente> | <ingrediente>
                PASOS: <paso> | <paso> | <paso>
                """.formatted(
                        course.tradition(),
                        order.describe(),
                        course.role(),
                        course.rules(),
                        String.join(", ", course.forbidden()));

        String reply = ask(prompt);
        course.fill(line(reply, "NOMBRE"),
                    split(line(reply, "INGREDIENTES")),
                    split(line(reply, "PASOS")));

        if (!course.isFilled()) {
            throw new IllegalStateException(
                    "el modelo no devolvio el formato pedido para " + course.role());
        }
    }

    @Override
    public String lastPrompt() {
        return lastPrompt;
    }

    @Override
    public String lastReply() {
        return lastReply;
    }

    // ------------------------------------------------------------------ HTTP

    private String ask(String prompt) {
        lastPrompt = prompt;
        String body = Json.object(
                Json.field("model", model),
                Json.field("prompt", prompt),
                Json.field("stream", false));

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "ollama contesto " + response.statusCode() + ": " + response.body());
            }
            lastReply = Json.value(response.body(), "response");
            return lastReply;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("no hay ollama en " + endpoint + " (" + e.getMessage() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("consulta interrumpida");
        }
    }

    // --------------------------------------------------------------- PARSING

    /** La linea que empieza con el prefijo, sin el prefijo. */
    private static String line(String reply, String prefix) {
        for (String raw : reply.split("\n")) {
            String clean = raw.replace("*", "").trim();
            if (clean.toUpperCase().startsWith(prefix + ":")) {
                return clean.substring(prefix.length() + 1).trim();
            }
        }
        return "";
    }

    private static List<String> split(String value) {
        List<String> parts = new ArrayList<>();
        for (String piece : value.split("\\|")) {
            String clean = piece.trim();
            if (!clean.isEmpty()) {
                parts.add(clean);
            }
        }
        return parts;
    }
}
