import java.util.List;

/**
 * JSON by hand. The JDK has no JSON class and the assignment is plain Java, so
 * there is no library: writing is easy, and the only reading needed is pulling
 * one text field out of Ollama's flat answer.
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
     * Pulls a text field out of a flat JSON object. That is enough because the
     * only answer to be read is Ollama's, which carries "response" near the top
     * and unnested.
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
