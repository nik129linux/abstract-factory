import java.util.List;

/**
 * JSON a mano. El JDK no trae una clase de JSON y el taller es en Java pelado,
 * asi que no hay libreria: escribir es facil, y de leer solo hace falta sacar
 * un campo de texto de la respuesta plana de Ollama.
 */
public final class Json {

    private Json() {
    }

    public static String str(String value) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : String.valueOf(value).toCharArray()) {
            if (c == '"' || c == '\\') {
                sb.append('\\').append(c);
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c < 0x20) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    public static String field(String name, String value) {
        return str(name) + ":" + str(value);
    }

    public static String field(String name, int value) {
        return str(name) + ":" + value;
    }

    public static String field(String name, boolean value) {
        return str(name) + ":" + value;
    }

    public static String field(String name, List<String> values) {
        StringBuilder sb = new StringBuilder(str(name)).append(":[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(str(values.get(i)));
        }
        return sb.append(']').toString();
    }

    public static String object(String... fields) {
        return "{" + String.join(",", fields) + "}";
    }

    /**
     * Saca un campo de texto de un objeto JSON plano. Alcanza porque la unica
     * respuesta que hay que leer es la de Ollama, que trae "response" arriba
     * del todo y sin anidar.
     */
    public static String value(String json, String field) {
        int at = json.indexOf("\"" + field + "\"");
        if (at < 0) {
            return "";
        }
        int colon = json.indexOf(':', at + field.length() + 2);
        int open = json.indexOf('"', colon + 1);
        if (colon < 0 || open < 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = open + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                break;
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char next = json.charAt(++i);
            switch (next) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                    i += 4;
                }
                default -> sb.append(next);
            }
        }
        return sb.toString();
    }
}
