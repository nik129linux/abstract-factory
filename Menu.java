import java.util.ArrayList;
import java.util.List;

/**
 * Los cuatro platos que salieron de UNA cocina.
 *
 * El constructor es el argumento entero del patron: pide la cocina una sola vez
 * y de ahi salen los cuatro. No hay ningun punto en el programa donde se pueda
 * colar un plato de otra familia, porque no hay ningun otro camino para crearlos.
 */
public class Menu {

    private final Kitchen kitchen;
    private final List<Course> courses;

    public Menu(Kitchen kitchen) {
        this.kitchen = kitchen;
        this.courses = new ArrayList<>(List.of(
                kitchen.createStarter(),
                kitchen.createMainCourse(),
                kitchen.createDessert(),
                kitchen.createDrink()));
    }

    /** Menu armado a mano, salteandose la fabrica. Solo para el contraejemplo. */
    public static Menu mixedByHand() {
        Menu menu = new Menu(new JapaneseKitchen());
        menu.courses.set(1, new ItalianMain());
        menu.courses.set(3, new ColombianDrink());
        return menu;
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

    /** Las tradiciones que hay realmente en el menu, en orden. */
    public List<String> traditionsUsed() {
        List<String> used = new ArrayList<>();
        for (Course course : courses) {
            used.add(course.tradition());
        }
        return used;
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
