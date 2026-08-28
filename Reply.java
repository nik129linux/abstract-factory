import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lee las respuestas del modelo.
 *
 * El formato es lineas con prefijo -- NOMBRE:, ACCION:, PRECIO: -- y no JSON.
 * A un modelo de 30B se le va una coma y no queda nada que parsear; un prefijo
 * al principio de la linea lo cumple siempre, y si escribe texto de mas
 * alrededor se ignora solo.
 */
public final class Reply {

    private Reply() {
    }

    /** La linea que empieza con el prefijo, sin el prefijo. "" si no esta. */
    public static String line(String reply, String prefix) {
        for (String raw : String.valueOf(reply).split("\n")) {
            String clean = raw.replace("*", "").replace("#", "").trim();
            if (clean.toUpperCase(Locale.ROOT).startsWith(prefix + ":")) {
                String value = clean.substring(prefix.length() + 1).trim();
                return value.equals("-") ? "" : value;
            }
        }
        return "";
    }

    /** Una linea de "a | b | c" partida en pedazos. */
    public static List<String> split(String value) {
        List<String> parts = new ArrayList<>();
        for (String piece : String.valueOf(value).split("\\|")) {
            String clean = piece.trim();
            if (!clean.isEmpty()) {
                parts.add(clean);
            }
        }
        return parts;
    }

    /** El primer entero que aparezca. 0 si no hay. Para precios y comensales. */
    public static int number(String value) {
        String digits = String.valueOf(value).replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits.length() > 9 ? digits.substring(0, 9) : digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
