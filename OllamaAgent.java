import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * El modelo de verdad, detras de ollama.
 *
 * java.net.http viene con el JDK desde Java 11, asi que sigue sin haber
 * libreria. Ollama expone /api/generate y con "stream": false contesta un solo
 * JSON plano, que es justo lo que Json.value sabe leer.
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
    public String ask(String prompt) {
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

    @Override
    public String lastPrompt() {
        return lastPrompt;
    }

    @Override
    public String lastReply() {
        return lastReply;
    }
}
