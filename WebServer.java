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
 * Serves the browser interface. The pattern lives in the Java classes; this is
 * only the wiring between them and the page.
 *
 * com.sun.net.httpserver ships with the JDK, so there is no library here either.
 *
 * A single endpoint does all the work: /api/say. The page sends whatever the
 * client typed and gets the whole state back -- what the waiter decided, how
 * the order stands, the combo with its price, and the record of what the chef
 * accepted and rejected while cooking.
 */
public class WebServer {

    private final int port;
    private final Path webDir;
    private final Agent agent;

    private final Conversation talk;

    public WebServer(int port, Path webDir, Agent agent) {
        this.port = port;
        this.webDir = webDir;
        this.agent = agent;
        this.talk = new Conversation(agent);
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
                case "state": send(exchange, 200, state());  return;
                case "say":   send(exchange, 200, say(params)); return;
                case "mixed": send(exchange, 200, mixed());  return;
                default: send(exchange, 404, Json.object(Json.field("error", "no such action: " + action)));
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

    /** One turn from the client. The whole system fits in this call. */
    private String say(Map<String, String> params) {
        String text = params.getOrDefault("text", "").trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("you typed nothing");
        }
        talk.say(text);

        Order order = talk.order();
        Combo combo = talk.combo();

        String comboJson = combo == null ? "null" : Json.object(
                Json.field("tradition", combo.kitchen().tradition()),
                Json.field("factory", combo.kitchen().getClass().getSimpleName()),
                Json.field("accent", combo.kitchen().accent()),
                Json.field("sameFamily", combo.sameFamily()),
                Json.field("used", combo.traditionsUsed()),
                Json.field("costPerPerson", combo.costPerPerson()),
                Json.field("total", combo.total(order)),
                Json.field("budget", order.budget()),
                Json.field("overBudget", order.budget() > 0 && combo.costPerPerson() > order.budget()),
                "\"courses\":[" + String.join(",", courses(combo)) + "]");

        return Json.object(
                Json.field("action", talk.lastAction()),
                Json.field("say", talk.lastSay()),
                Json.field("order", order.describe()),
                Json.field("complete", order.isComplete()),
                Json.field("log", talk.log()),
                "\"combo\":" + comboJson);
    }

    private List<String> courses(Combo combo) {
        List<String> out = new ArrayList<>();
        for (int n = 1; n <= combo.size(); n++) {
            Course course = combo.course(n);
            out.add(Json.object(
                    Json.field("n", n),
                    Json.field("role", course.role()),
                    Json.field("clazz", course.getClass().getSimpleName()),
                    Json.field("name", course.name()),
                    Json.field("ingredients", course.ingredients()),
                    Json.field("steps", course.steps()),
                    Json.field("cost", course.cost()),
                    Json.field("violations", course.violations()),
                    Json.field("missing", course.missing())));
        }
        return out;
    }

    /**
     * The counter-example: a combo built by hand with new, out of three
     * different kitchens. It compiles all the same. What prevents it everywhere
     * else is not a validation, it is that there is no other way to create a
     * dish.
     */
    private String mixed() {
        Combo byHand = Combo.mixedByHand();
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
