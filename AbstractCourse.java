import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The part that is identical in all twelve dishes: they all hold a name, a list
 * of ingredients and a list of steps once the agent fills them in.
 *
 * What is NOT here is what makes each dish its own class: role(), tradition(),
 * rules() and forbidden(). That is the pattern; this is bookkeeping, and
 * repeating it twelve times would prove nothing.
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
     * The agent answers with free text, so it can promise one thing and write
     * another. This checks what it wrote against the family's blacklist.
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
     * What the model made up. It is grounds for rejection: if it is not in the
     * warehouse it cannot be cooked and it cannot be priced.
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

    /** A dish costs the sum of its ingredients, and nothing else. */
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
        return role() + " " + tradition() + ": " + (isFilled() ? name : "(empty)");
    }
}
