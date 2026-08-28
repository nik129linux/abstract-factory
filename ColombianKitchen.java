import java.util.List;

/** Concrete factory. */
public class ColombianKitchen implements Kitchen {

    public static final String TRADITION = "colombian";

    /** What all four classes of this family are barred from, in one place. */
    static final List<String> FORBIDDEN = List.of("soy", "wasabi", "matcha", "parmesan", "risotto", "prosciutto");

    @Override
    public String tradition() {
        return TRADITION;
    }

    @Override
    public String accent() {
        return "colombian cooking: corn, plantain, tubers and a sofrito of onion and tomato; long cooking, panela for sweetness, flavour without aggressive heat";
    }

    @Override
    public Starter createStarter() {
        return new ColombianStarter();
    }

    @Override
    public MainCourse createMainCourse() {
        return new ColombianMain();
    }

    @Override
    public Dessert createDessert() {
        return new ColombianDessert();
    }

    @Override
    public Drink createDrink() {
        return new ColombianDrink();
    }
}
