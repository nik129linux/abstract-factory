import java.util.List;

/** Producto concreto: postre italiano. */
public class ItalianDessert extends AbstractCourse implements Dessert {

    @Override
    public String tradition() {
        return ItalianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "crema, cafe o fruta de temporada; se arma en capas o en copa, "
             + "reposa en frio antes de servir";
    }

    @Override
    public List<String> forbidden() {
        return ItalianKitchen.FORBIDDEN;
    }
}
