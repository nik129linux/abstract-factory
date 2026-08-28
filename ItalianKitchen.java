import java.util.List;

/** Concrete factory. */
public class ItalianKitchen implements Kitchen {

    public static final String TRADITION = "italian";

    /** What all four classes of this family are barred from, in one place. */
    static final List<String> FORBIDDEN = List.of("soy", "wasabi", "matcha", "panela", "arepa", "curry");

    @Override
    public String tradition() {
        return TRADITION;
    }

    @Override
    public String accent() {
        return "italian cooking: few good ingredients, olive oil, fresh herbs, pasta or dough made in house, regional cheeses";
    }

    @Override
    public Starter createStarter() {
        return new ItalianStarter();
    }

    @Override
    public MainCourse createMainCourse() {
        return new ItalianMain();
    }

    @Override
    public Dessert createDessert() {
        return new ItalianDessert();
    }

    @Override
    public Drink createDrink() {
        return new ItalianDrink();
    }
}
