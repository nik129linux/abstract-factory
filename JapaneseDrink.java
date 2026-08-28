import java.util.List;

/** Producto concreto: bebida japonesa. */
public class JapaneseDrink extends AbstractCourse implements Drink {

    @Override
    public String tradition() {
        return JapaneseKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "te verde, cebada tostada o sake; se sirve en taza o tokkuri, "
             + "sin azucar anadida";
    }

    @Override
    public List<String> forbidden() {
        return JapaneseKitchen.FORBIDDEN;
    }
}
