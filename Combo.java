import java.util.ArrayList;
import java.util.List;

/**
 * THE COMBO: the four dishes that came out of ONE kitchen, with their price.
 *
 * The constructor is the whole argument for the pattern: it asks for the
 * kitchen once and the four dishes come out of it. There is no point in the
 * program where a dish from another family can slip in, because there is no
 * other way to create one.
 */
public class Combo {

    private final Kitchen kitchen;
    private final List<Course> courses;

    public Combo(Kitchen kitchen) {
        this.kitchen = kitchen;
        this.courses = new ArrayList<>(List.of(
                kitchen.createStarter(),
                kitchen.createMainCourse(),
                kitchen.createDessert(),
                kitchen.createDrink()));
    }

    /** A combo built by hand, going around the factory. Counter-example only. */
    public static Combo mixedByHand() {
        Combo combo = new Combo(new JapaneseKitchen());
        combo.courses.set(1, new ItalianMain());
        combo.courses.set(3, new ColombianDrink());
        return combo;
    }

    public Kitchen kitchen() {
        return kitchen;
    }

    public List<Course> courses() {
        return List.copyOf(courses);
    }

    /** n runs from 1 to 4, like the steps in the interface. */
    public Course course(int n) {
        if (n < 1 || n > courses.size()) {
            throw new IllegalArgumentException("there is no course " + n);
        }
        return courses.get(n - 1);
    }

    public int size() {
        return courses.size();
    }

    /** What the factory guarantees: all four share one tradition. */
    public boolean sameFamily() {
        for (Course course : courses) {
            if (!course.tradition().equals(kitchen.tradition())) {
                return false;
            }
        }
        return true;
    }

    /** The traditions actually present in the combo, in order. */
    public List<String> traditionsUsed() {
        List<String> used = new ArrayList<>();
        for (Course course : courses) {
            used.add(course.tradition());
        }
        return used;
    }

    /** Throws course n away and puts an empty one from the SAME kitchen. */
    public void redo(int n) {
        Course fresh = switch (n) {
            case 1 -> kitchen.createStarter();
            case 2 -> kitchen.createMainCourse();
            case 3 -> kitchen.createDessert();
            case 4 -> kitchen.createDrink();
            default -> throw new IllegalArgumentException("there is no course " + n);
        };
        courses.set(n - 1, fresh);
    }

    /** The priciest dish: the one to redo when the combo will not fit the budget. */
    public int priciest() {
        int worst = 1;
        for (int n = 1; n <= courses.size(); n++) {
            if (course(n).cost() > course(worst).cost()) {
                worst = n;
            }
        }
        return worst;
    }

    /** COP per person. Arithmetic over the pantry, not a figure from the model. */
    public int costPerPerson() {
        int total = 0;
        for (Course course : courses) {
            total += course.cost();
        }
        return total;
    }

    public int total(Order order) {
        return costPerPerson() * order.diners();
    }

    /** Whether the chef has cooked every course. */
    public boolean isReady() {
        for (Course course : courses) {
            if (!course.isFilled()) {
                return false;
            }
        }
        return true;
    }

    /** Ingredients the model made up that the warehouse does not stock. */
    public List<String> missing() {
        List<String> all = new ArrayList<>();
        for (Course course : courses) {
            for (String item : course.missing()) {
                all.add(course.role() + ": " + item);
            }
        }
        return all;
    }

    /** What the agent wrote and should not have. Different from sameFamily(). */
    public List<String> violations() {
        List<String> all = new ArrayList<>();
        for (Course course : courses) {
            for (String word : course.violations()) {
                all.add(course.role() + ": " + word);
            }
        }
        return all;
    }
}
