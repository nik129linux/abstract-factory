import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * The real model, behind ollama.
 *
 * java.net.http ships with the JDK since Java 11, so there is still no library
 * here. Ollama exposes /api/generate and with "stream": false it answers with a
 * single flat JSON object, which is exactly what Json.value knows how to read.
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
                        "ollama answered " + response.statusCode() + ": " + response.body());
            }
            lastReply = Json.value(response.body(), "response");
            return lastReply;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("no ollama at " + endpoint + " (" + e.getMessage() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("request interrupted");
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
