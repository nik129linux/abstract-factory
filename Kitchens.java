import java.util.List;
import java.util.Locale;

/**
 * The only place in the program where a "new XKitchen()" appears.
 *
 * The agent answers with a word; this turns it into a factory. Adding a fourth
 * kitchen means writing its five classes and adding it to this list: the agent,
 * the combo and the server never find out.
 */
public final class Kitchens {

    private Kitchens() {
    }

    public static List<Kitchen> all() {
        return List.of(new JapaneseKitchen(), new ItalianKitchen(), new ColombianKitchen());
    }

    public static List<String> traditions() {
        return all().stream().map(Kitchen::tradition).toList();
    }

    /** Forgiving on purpose: the model answers with text, not with an enum. */
    public static Kitchen byTradition(String answer) {
        String clean = answer == null ? "" : answer.toLowerCase(Locale.ROOT);
        for (Kitchen kitchen : all()) {
            if (clean.contains(kitchen.tradition())) {
                return kitchen;
            }
        }
        throw new IllegalArgumentException("the agent named no kitchen: " + answer);
    }
}
