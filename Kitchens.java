import java.util.List;
import java.util.Locale;

/**
 * El unico lugar del programa donde aparece un "new XKitchen()".
 *
 * El agente contesta una palabra; esto la convierte en una fabrica. Meter una
 * cuarta cocina es escribir sus cinco clases y agregarla a esta lista: ni el
 * agente, ni el Menu, ni el servidor se enteran.
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

    /** Tolerante a proposito: el modelo contesta texto, no un enum. */
    public static Kitchen byTradition(String answer) {
        String clean = answer == null ? "" : answer.toLowerCase(Locale.ROOT);
        for (Kitchen kitchen : all()) {
            if (clean.contains(kitchen.tradition())) {
                return kitchen;
            }
        }
        throw new IllegalArgumentException("el agente no nombro ninguna cocina: " + answer);
    }
}
