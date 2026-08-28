import java.util.ArrayList;
import java.util.List;

/**
 * EL COMBO: los cuatro platos que salieron de UNA cocina, con su precio.
 *
 * El constructor es el argumento entero del patron: pide la cocina una sola vez
 * y de ahi salen los cuatro. No hay ningun punto en el programa donde se pueda
 * colar un plato de otra familia, porque no hay ningun otro camino para crearlos.
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

    /** Combo armado a mano, salteandose la fabrica. Solo para el contraejemplo. */
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

    /** n va de 1 a 4, como los pasos en la interfaz. */
    public Course course(int n) {
        if (n < 1 || n > courses.size()) {
            throw new IllegalArgumentException("no hay plato " + n);
        }
        return courses.get(n - 1);
    }

    public int size() {
        return courses.size();
    }

    /** Lo que la fabrica garantiza: los cuatro son de la misma tradicion. */
    public boolean sameFamily() {
        for (Course course : courses) {
            if (!course.tradition().equals(kitchen.tradition())) {
                return false;
            }
        }
        return true;
    }

    /** Las tradiciones que hay realmente en el combo, en orden. */
    public List<String> traditionsUsed() {
        List<String> used = new ArrayList<>();
        for (Course course : courses) {
            used.add(course.tradition());
        }
        return used;
    }

    /** Bota el plato n y pone uno vacio recien salido de la MISMA cocina. */
    public void redo(int n) {
        Course fresh = switch (n) {
            case 1 -> kitchen.createStarter();
            case 2 -> kitchen.createMainCourse();
            case 3 -> kitchen.createDessert();
            case 4 -> kitchen.createDrink();
            default -> throw new IllegalArgumentException("no hay plato " + n);
        };
        courses.set(n - 1, fresh);
    }

    /** El plato mas caro, que es el que hay que rehacer si no cabe el presupuesto. */
    public int priciest() {
        int worst = 1;
        for (int n = 1; n <= courses.size(); n++) {
            if (course(n).cost() > course(worst).cost()) {
                worst = n;
            }
        }
        return worst;
    }

    /** COP por persona. Aritmetica sobre la despensa, no una cifra del modelo. */
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

    /** Cuantos platos ya cocino el chef. */
    public boolean isReady() {
        for (Course course : courses) {
            if (!course.isFilled()) {
                return false;
            }
        }
        return true;
    }

    /** Ingredientes que el modelo se invento y no estan en la bodega. */
    public List<String> missing() {
        List<String> all = new ArrayList<>();
        for (Course course : courses) {
            for (String item : course.missing()) {
                all.add(course.role() + ": " + item);
            }
        }
        return all;
    }

    /** Lo que el agente escribio y no debia. Otra cosa distinta a sameFamily(). */
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
