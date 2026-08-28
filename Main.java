import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Assignment 3: Abstract Factory driven by an AI agent.
 *
 * Case study: a catering service. Each culinary tradition is a family of
 * products that cannot be mixed, and the agent is the one that talks to the
 * client, picks the family and decides the dishes, with nobody dictating them.
 *
 *   javac *.java
 *   java Main                       ollama agent, gemma4:31b-cloud
 *   java Main --agent=offline       no network, for presenting without internet
 *   java Main --model=llama3.2      another ollama model
 */
public class Main {

    private static final int PORT = 8081;

    public static void main(String[] args) throws Exception {
        String kind = arg(args, "--agent", "ollama");
        String model = arg(args, "--model", OllamaAgent.DEFAULT_MODEL);
        String host = arg(args, "--host", "http://localhost:11434");

        Agent agent = kind.equals("offline")
                ? new OfflineAgent()
                : new OllamaAgent(model, host);

        Path webDir = Paths.get("web").toAbsolutePath().normalize();
        new WebServer(PORT, webDir, agent).start();

        String url = "http://localhost:" + PORT;
        System.out.println("Catering at " + url);
        System.out.println("Agent: " + agent.name());
        System.out.println("Ctrl+C to stop.");

        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.out.println("Open that address by hand.");
        }
    }

    private static String arg(String[] args, String name, String fallback) {
        for (String raw : args) {
            if (raw.startsWith(name + "=")) {
                return raw.substring(name.length() + 1);
            }
        }
        return fallback;
    }
}
