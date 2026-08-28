import java.util.List;

/** Producto concreto: postre colombiano. */
public class ColombianDessert extends AbstractCourse implements Dessert {

    @Override
    public String tradition() {
        return ColombianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "dulce de panela o de fruta cocida, a veces con queso fresco al lado; "
             + "se sirve tibio o al clima";
    }

    @Override
    public List<String> forbidden() {
        return ColombianKitchen.FORBIDDEN;
    }
}
