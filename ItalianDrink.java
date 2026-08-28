import java.util.List;

/** Producto concreto: bebida italiana. */
public class ItalianDrink extends AbstractCourse implements Drink {

    @Override
    public String tradition() {
        return ItalianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "vino de una region italiana, un aperitivo amargo o un cafe corto; "
             + "se nombra la region, no la marca";
    }

    @Override
    public List<String> forbidden() {
        return ItalianKitchen.FORBIDDEN;
    }
}
