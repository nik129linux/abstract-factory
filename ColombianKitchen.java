import java.util.List;

/** Fabrica concreta 3 de 3. */
public class ColombianKitchen implements Kitchen {

    public static final String TRADITION = "colombiana";

    static final List<String> FORBIDDEN =
            List.of("soya", "wasabi", "matcha", "parmesano", "risotto", "prosciutto");

    @Override
    public String tradition() {
        return TRADITION;
    }

    @Override
    public String accent() {
        return "cocina colombiana: maiz, platano, tuberculos y guiso de cebolla y tomate; "
             + "cocciones largas, panela como dulce, sabor sin picante agresivo";
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
