import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sirve la interfaz del navegador. El patron vive en las clases de Java; esto
 * es solo el cable entre ellas y la pagina.
 *
 * com.sun.net.httpserver viene con el JDK, asi que aca tampoco hay libreria.
 *
 * Los platos se llenan de a uno, un request por plato, para que se vea al
 * agente trabajando en vez de que aparezca el menu completo de golpe.
 */
public class WebServer {

    private final int port;
    private final Path webDir;
    private final Agent agent;

    private Menu menu;     // el menu en curso
    private Order order;   // el pedido que lo origino

    public WebServer(int port, Path webDir, Agent agent) {
        this.port = port;
        this.webDir = webDir;
        this.agent = agent;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/", this::handleApi);
        server.createContext("/", this::handleStatic);
        server.start();
    }

    // ------------------------------------------------------------------- API

    private void handleApi(HttpExchange exchange) throws IOException {
        String action = exchange.getRequestURI().getPath().substring("/api/".length());
        Map<String, String> params = query(exchange.getRequestURI().getRawQuery());

        try {
            switch (action) {
                case "state":  send(exchange, 200, state());          return;
                case "choose": send(exchange, 200, choose(params));   return;
                case "fill":   send(exchange, 200, fill(params));     return;
                case "check":  send(exchange, 200, check());          return;
                case "mixed":  send(exchange, 200, mixed());          return;
                default: send(exchange, 404, Json.object(Json.field("error", "no existe " + action)));
            }
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.toString() : e.getMessage();
            send(exchange, 400, Json.object(Json.field("error", message)));
        }
    }

    private String state() {
        return Json.object(
                Json.field("agent", agent.name()),
                Json.field("traditions", Kitchens.traditions()));
    }

    /**
     * Paso 1. El agente lee el pedido y contesta una palabra; esa palabra elige
     * la fabrica, y la fabrica sola arma los cuatro platos vacios.
     */
    private String choose(Map<String, String> params) {
        Order order = Order.of(
                params.getOrDefault("diners", "2"),
                params.getOrDefault("restrictions", ""),
                params.getOrDefault("occasion", ""),
                params.getOrDefault("notes", ""));

        String answer = agent.chooseTradition(order, Kitchens.traditions());
        Kitchen kitchen = Kitchens.byTradition(answer);
        menu = new Menu(kitchen);
        this.order = order;

        List<String> slots = new ArrayList<>();
        for (int n = 1; n <= menu.size(); n++) {
            Course course = menu.course(n);
            slots.add(Json.object(
                    Json.field("n", n),
                    Json.field("role", course.role()),
                    Json.field("rules", course.rules()),
                    Json.field("forbidden", course.forbidden())));
        }

        return Json.object(
                Json.field("answer", answer),
                Json.field("tradition", kitchen.tradition()),
                Json.field("accent", kitchen.accent()),
                Json.field("factory", kitchen.getClass().getSimpleName()),
                Json.field("order", order.describe()),
                Json.field("prompt", agent.lastPrompt()),
                Json.field("reply", agent.lastReply()),
                "\"courses\":[" + String.join(",", slots) + "]");
    }

    /** Paso 2, una vez por plato. */
    private String fill(Map<String, String> params) {
        if (menu == null) {
            throw new IllegalStateException("todavia no hay pedido");
        }
        int n = Integer.parseInt(params.getOrDefault("n", "1"));
        Course course = menu.course(n);
        agent.fill(course, order);

        return Json.object(
                Json.field("n", n),
                Json.field("role", course.role()),
                Json.field("tradition", course.tradition()),
                Json.field("clazz", course.getClass().getSimpleName()),
                Json.field("name", course.name()),
                Json.field("ingredients", course.ingredients()),
                Json.field("steps", course.steps()),
                Json.field("violations", course.violations()),
                Json.field("prompt", agent.lastPrompt()),
                Json.field("reply", agent.lastReply()),
                Json.field("done", n == menu.size()));
    }

    /** Paso 3. Las dos revisiones, que no son la misma. */
    private String check() {
        if (menu == null) {
            throw new IllegalStateException("todavia no hay pedido");
        }
        return Json.object(
                Json.field("sameFamily", menu.sameFamily()),
                Json.field("tradition", menu.kitchen().tradition()),
                Json.field("used", menu.traditionsUsed()),
                Json.field("violations", menu.violations()));
    }

    /**
     * El contraejemplo: un menu armado a mano con new de tres cocinas distintas.
     * Compila igual. Lo que lo impide en el resto del programa no es una
     * validacion, es que no existe otro camino para crear un plato.
     */
    private String mixed() {
        Menu byHand = Menu.mixedByHand();
        List<String> classes = new ArrayList<>();
        for (Course course : byHand.courses()) {
            classes.add(course.getClass().getSimpleName());
        }
        return Json.object(
                Json.field("sameFamily", byHand.sameFamily()),
                Json.field("tradition", byHand.kitchen().tradition()),
                Json.field("used", byHand.traditionsUsed()),
                Json.field("classes", classes));
    }

    // ---------------------------------------------------------------- STATIC

    private void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        Path file = webDir.resolve(path.substring(1)).normalize();
        if (!file.startsWith(webDir) || !Files.exists(file)) {
            send(exchange, 404, "not found");
            return;
        }

        exchange.getResponseHeaders().add("Content-Type", contentType(file.toString()));
        byte[] body = Files.readAllBytes(file);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private String contentType(String name) {
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (name.endsWith(".js")) {
            return "text/javascript; charset=utf-8";
        }
        return "text/plain; charset=utf-8";
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private Map<String, String> query(String raw) {
        Map<String, String> params = new HashMap<>();
        if (raw == null) {
            return params;
        }
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                params.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                           URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
            }
        }
        return params;
    }
}
