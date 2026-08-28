import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Taller 3: Abstract Factory con un agente de IA.
 *
 * Caso de estudio: un servicio de catering. Cada tradicion culinaria es una
 * familia de productos que no se puede mezclar, y el agente es el que elige la
 * familia y decide los platos, sin que nadie se lo dicte.
 *
 *   javac *.java
 *   java Main                        agente con ollama, modelo gemma4:31b-cloud
 *   java Main --agente=offline       sin red, para sustentar sin internet
 *   java Main --modelo=llama3.2      otro modelo de ollama
 */
public class Main {

    private static final int PORT = 8081;

    public static void main(String[] args) throws Exception {
        String kind = arg(args, "--agente", "ollama");
        String model = arg(args, "--modelo", OllamaAgent.DEFAULT_MODEL);
        String host = arg(args, "--host", "http://localhost:11434");

        Agent agent = kind.equals("offline")
                ? new OfflineAgent()
                : new OllamaAgent(model, host);

        Path webDir = Paths.get("web").toAbsolutePath().normalize();
        new WebServer(PORT, webDir, agent).start();

        String url = "http://localhost:" + PORT;
        System.out.println("Catering en " + url);
        System.out.println("Agente: " + agent.name());
        System.out.println("Ctrl+C para parar.");

        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.out.println("Abri esa direccion a mano.");
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
