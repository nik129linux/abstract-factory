import java.util.List;

/** Concrete factory. */
public class JapaneseKitchen implements Kitchen {

    public static final String TRADITION = "japanese";

    /** What all four classes of this family are barred from, in one place. */
    static final List<String> FORBIDDEN = List.of("cheese", "cream", "butter", "chorizo", "arepa", "curry");

    @Override
    public String tradition() {
        return TRADITION;
    }

    @Override
    public String accent() {
        return "japanese cooking: raw or barely cooked produce, dashi, umami, small portions, no dairy and no heavy heat";
    }

    @Override
    public Starter createStarter() {
        return new JapaneseStarter();
    }

    @Override
    public MainCourse createMainCourse() {
        return new JapaneseMain();
    }

    @Override
    public Dessert createDessert() {
        return new JapaneseDessert();
    }

    @Override
    public Drink createDrink() {
        return new JapaneseDrink();
    }
}
