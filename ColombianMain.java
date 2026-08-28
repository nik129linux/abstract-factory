import java.util.List;

/** Producto concreto: plato fuerte colombiano. */
public class ColombianMain extends AbstractCourse implements MainCourse {

    @Override
    public String tradition() {
        return ColombianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "guiso de base, un almidon y una proteina en el mismo plato; "
             + "coccion larga, se monta en plato hondo o bandeja";
    }

    @Override
    public List<String> forbidden() {
        return ColombianKitchen.FORBIDDEN;
    }
}
