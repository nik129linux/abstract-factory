import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * LA DESPENSA. Singleton -- hay una sola bodega, como el dispensador de turnos
 * del taller 1.
 *
 * Es la pieza que hace que el modelo choque con algo real. Sin esto el agente
 * escribe texto y nadie lo contradice; con esto cada ingrediente que nombra
 * tiene que existir aca y tiene un precio, y ESE precio es el del combo.
 *
 * El precio nunca sale del modelo. Un chatbot te diria "eso le sale como
 * doscientos mil"; aca se suma la despensa.
 */
public final class Pantry {

    private static final Pantry INSTANCE = new Pantry();

    /** COP por porcion. */
    private final Map<String, Integer> prices = new LinkedHashMap<>();
    private final Map<String, List<String>> byTradition = new LinkedHashMap<>();

    private Pantry() {
        stock(JapaneseKitchen.TRADITION,
                "arroz japones", 1200, "fideo soba", 2200, "salmon", 9800, "atun", 11500,
                "pulpo", 8600, "camaron", 7400, "tofu", 2600, "huevo", 900,
                "alga nori", 1500, "alga wakame", 1800, "pepino", 700, "rabano", 900,
                "jengibre", 600, "cebollin", 500, "salsa de soya", 700, "miso", 1400,
                "vinagre de arroz", 600, "ajonjoli", 500, "matcha", 3200,
                "frijol rojo dulce", 2100, "arroz glutinoso", 1600, "te verde", 900,
                "cebada tostada", 700, "sake", 6500, "dashi", 1100, "shiitake", 3400);

        stock(ItalianKitchen.TRADITION,
                "espagueti", 1400, "penne", 1400, "arroz arborio", 2400, "harina", 700,
                "tomate", 1100, "albahaca", 600, "ajo", 300, "cebolla", 500,
                "aceite de oliva", 1900, "pecorino", 5200, "parmesano", 5600,
                "mozzarella", 4300, "mascarpone", 5900, "prosciutto", 8900,
                "pan rustico", 1300, "alcaparra", 1200, "aceituna", 1600,
                "pollo", 6200, "res", 9400, "champinon", 3100, "crema", 2300,
                "cafe", 1500, "cacao", 1800, "vino tinto", 7800, "limon", 500);

        stock(ColombianKitchen.TRADITION,
                "maiz tierno", 1300, "harina de maiz", 800, "platano", 900, "yuca", 800,
                "papa", 700, "arroz", 800, "frijol", 1600, "arveja", 1400,
                "pollo", 6200, "res", 9400, "cerdo", 7100, "pescado de rio", 8300,
                "huevo", 900, "quesito", 3600, "cebolla larga", 500, "tomate", 1100,
                "cilantro", 400, "guiso", 1200, "aguacate", 2600, "panela", 700,
                "arequipe", 2900, "breva", 2200, "mora", 1500, "lulo", 1700,
                "limon", 500, "cafe", 1500, "mazorca", 1400, "costilla", 8700);
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

    /** Lo que se le ofrece al agente en el prompt, con precio. */
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

    /** 0 si no esta. Quien pregunta decide que hacer con eso. */
    public int price(String written) {
        String key = match(written);
        return key == null ? 0 : prices.get(key);
    }

    /**
     * El modelo escribe "Pulpo cocido en dashi", no "pulpo". Se normaliza y se
     * busca cual ingrediente de la despensa aparece adentro de lo que escribio.
     * Gana el nombre mas largo, para que "cebolla larga" no la resuelva "cebolla".
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
