import java.util.List;

/** Producto concreto: entrada colombiana. */
public class ColombianStarter extends AbstractCourse implements Starter {

    @Override
    public String tradition() {
        return ColombianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "algo de maiz o platano, o un caldo corto; se sirve caliente "
             + "y va acompanado de aji aparte, nunca encima";
    }

    @Override
    public List<String> forbidden() {
        return ColombianKitchen.FORBIDDEN;
    }
}
