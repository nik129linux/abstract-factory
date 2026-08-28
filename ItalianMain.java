import java.util.List;

/** Producto concreto: plato fuerte italiano. */
public class ItalianMain extends AbstractCourse implements MainCourse {

    @Override
    public String tradition() {
        return ItalianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "pasta, risotto o horno; la salsa se hace con lo que ya esta en el plato, "
             + "el queso se ralla al final";
    }

    @Override
    public List<String> forbidden() {
        return ItalianKitchen.FORBIDDEN;
    }
}
