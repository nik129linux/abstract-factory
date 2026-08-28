import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads the model's answers.
 *
 * The format is prefixed lines -- NAME:, ACTION:, STEPS: -- and not JSON. A 30B
 * model drops a comma and then there is nothing left to parse; a prefix at the
 * start of a line it always gets right, and any extra text around it is
 * ignored for free.
 */
public final class Reply {

    private Reply() {
    }

    /** The line starting with the prefix, without the prefix. "" if absent. */
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

    /** A line of "a | b | c" split into pieces. */
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

    /** The first integer in the text. 0 if there is none. For prices and counts. */
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
