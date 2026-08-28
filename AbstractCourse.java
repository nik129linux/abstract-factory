import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * La parte que es identica en los doce platos: todos guardan un nombre, unos
 * ingredientes y unos pasos cuando el agente los llena.
 *
 * Lo que NO esta aca es lo que hace que cada plato sea su propia clase:
 * role(), tradition(), rules() y forbidden(). Eso es el patron; esto es
 * contabilidad, y repetirlo doce veces no probaria nada.
 */
public abstract class AbstractCourse implements Course {

    private String name = "";
    private final List<String> ingredients = new ArrayList<>();
    private final List<String> steps = new ArrayList<>();

    @Override
    public void fill(String name, List<String> ingredients, List<String> steps) {
        this.name = name == null ? "" : name.trim();
        this.ingredients.clear();
        this.steps.clear();
        if (ingredients != null) {
            this.ingredients.addAll(ingredients);
        }
        if (steps != null) {
            this.steps.addAll(steps);
        }
    }

    @Override
    public boolean isFilled() {
        return !name.isEmpty();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<String> ingredients() {
        return List.copyOf(ingredients);
    }

    @Override
    public List<String> steps() {
        return List.copyOf(steps);
    }

    /**
     * El agente contesta texto libre, asi que puede prometer una cosa y escribir
     * otra. Esto revisa lo que escribio contra la lista negra de la familia.
     */
    @Override
    public List<String> violations() {
        String written = (name + " " + String.join(" ", ingredients)).toLowerCase(Locale.ROOT);
        List<String> found = new ArrayList<>();
        for (String word : forbidden()) {
            if (written.contains(word.toLowerCase(Locale.ROOT))) {
                found.add(word);
            }
        }
        return found;
    }

    /**
     * Lo que el modelo se invento. Es motivo de rechazo: si no esta en la
     * bodega, no se puede cocinar ni cotizar.
     */
    @Override
    public List<String> missing() {
        List<String> gone = new ArrayList<>();
        for (String item : ingredients) {
            if (!Pantry.get().has(item)) {
                gone.add(item);
            }
        }
        return gone;
    }

    /** El precio del plato es la suma de sus ingredientes, y nada mas. */
    @Override
    public int cost() {
        int total = 0;
        for (String item : ingredients) {
            total += Pantry.get().price(item);
        }
        return total;
    }

    @Override
    public String toString() {
        return role() + " " + tradition() + ": " + (isFilled() ? name : "(vacia)");
    }
}
