import java.util.List;

/** Fabrica concreta 1 de 3. */
public class JapaneseKitchen implements Kitchen {

    public static final String TRADITION = "japonesa";

    /** Lo que las cuatro clases de esta familia tienen prohibido, en un lugar. */
    static final List<String> FORBIDDEN =
            List.of("queso", "crema de leche", "mantequilla", "chorizo", "arepa", "aji");

    @Override
    public String tradition() {
        return TRADITION;
    }

    @Override
    public String accent() {
        return "cocina japonesa: producto crudo o apenas cocido, dashi, umami, "
             + "porciones pequenas, sin lacteos y sin picante fuerte";
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
