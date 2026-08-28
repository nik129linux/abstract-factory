import java.util.List;

/** Fabrica concreta 2 de 3. */
public class ItalianKitchen implements Kitchen {

    public static final String TRADITION = "italiana";

    static final List<String> FORBIDDEN =
            List.of("soya", "wasabi", "matcha", "panela", "arepa", "curry");

    @Override
    public String tradition() {
        return TRADITION;
    }

    @Override
    public String accent() {
        return "cocina italiana: pocos ingredientes de buena calidad, aceite de oliva, "
             + "hierbas frescas, pasta o masa hecha en casa, quesos de la region";
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
