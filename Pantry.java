import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * THE PANTRY. A singleton -- there is only one warehouse, like the ticket
 * dispenser in assignment 1.
 *
 * This is the piece that makes the model hit something real. Without it the
 * agent writes text and nobody contradicts it; with it every ingredient it
 * names has to exist here and carries a price, and THAT price is the combo's.
 *
 * The price never comes from the model. A chatbot would say "that runs you
 * about two hundred thousand"; here the pantry is added up.
 */
public final class Pantry {

    private static final Pantry INSTANCE = new Pantry();

    /** COP per portion. */
    private final Map<String, Integer> prices = new LinkedHashMap<>();
    private final Map<String, List<String>> byTradition = new LinkedHashMap<>();

    private Pantry() {
        stock(JapaneseKitchen.TRADITION,
                "japanese rice", 1200, "soba noodles", 2200, "salmon", 9800, "tuna", 11500,
                "octopus", 8600, "shrimp", 7400, "tofu", 2600, "egg", 900,
                "nori seaweed", 1500, "wakame seaweed", 1800, "cucumber", 700, "radish", 900,
                "ginger", 600, "spring onion", 500, "soy sauce", 700, "miso", 1400,
                "rice vinegar", 600, "sesame", 500, "matcha", 3200,
                "sweet red bean", 2100, "glutinous rice", 1600, "green tea", 900,
                "roasted barley", 700, "sake", 6500, "dashi", 1100, "shiitake", 3400);

        stock(ItalianKitchen.TRADITION,
                "spaghetti", 1400, "penne", 1400, "arborio rice", 2400, "flour", 700,
                "tomato", 1100, "basil", 600, "garlic", 300, "onion", 500,
                "olive oil", 1900, "pecorino", 5200, "parmesan", 5600,
                "mozzarella", 4300, "mascarpone", 5900, "prosciutto", 8900,
                "rustic bread", 1300, "caper", 1200, "olive", 1600,
                "chicken", 6200, "beef", 9400, "mushroom", 3100, "cream", 2300,
                "coffee", 1500, "cocoa", 1800, "red wine", 7800, "lemon", 500);

        stock(ColombianKitchen.TRADITION,
                "sweet corn", 1300, "corn flour", 800, "plantain", 900, "cassava", 800,
                "potato", 700, "rice", 800, "beans", 1600, "peas", 1400,
                "chicken", 6200, "beef", 9400, "pork", 7100, "river fish", 8300,
                "egg", 900, "fresh cheese", 3600, "long onion", 500, "tomato", 1100,
                "coriander", 400, "sofrito", 1200, "avocado", 2600, "panela", 700,
                "arequipe", 2900, "fig", 2200, "blackberry", 1500, "lulo", 1700,
                "lemon", 500, "coffee", 1500, "corn cob", 1400, "ribs", 8700);
    }

    public static Pantry get() {
        return INSTANCE;
    }

    private void stock(String tradition, Object... pairs) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < pairs.length; i += 2) {
            String name = (String) pairs[i];
            prices.put(name, (Integer) pairs[i + 1]);
            list.add(name);
        }
        byTradition.put(tradition, list);
    }

    /** What the agent is offered in the prompt, with prices. */
    public List<String> forTradition(String tradition) {
        return List.copyOf(byTradition.getOrDefault(tradition, List.of()));
    }

    public String priceList(String tradition) {
        StringBuilder sb = new StringBuilder();
        for (String name : forTradition(tradition)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name).append(" $").append(prices.get(name));
        }
        return sb.toString();
    }

    public boolean has(String written) {
        return match(written) != null;
    }

    /** 0 when it is not stocked. The caller decides what to do about that. */
    public int price(String written) {
        String key = match(written);
        return key == null ? 0 : prices.get(key);
    }

    /**
     * The model writes "octopus poached in dashi", not "octopus". The text is
     * normalised and searched for whichever pantry entry appears inside it. The
     * longest name wins, so that "long onion" is not resolved by "onion".
     */
    public String match(String written) {
        String clean = normalize(written);
        if (clean.isEmpty()) {
            return null;
        }
        String best = null;
        for (String name : prices.keySet()) {
            if (clean.contains(normalize(name)) && (best == null || name.length() > best.length())) {
                best = name;
            }
        }
        return best;
    }

    private static String normalize(String value) {
        String plain = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return plain.toLowerCase(Locale.ROOT).trim();
    }
}
