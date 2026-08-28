import java.util.List;

/** Concrete product: Colombian dessert. */
public class ColombianDessert extends AbstractCourse implements Dessert {

    @Override
    public String tradition() {
        return ColombianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "panela or cooked fruit sweet, sometimes with fresh cheese alongside; served warm or at room temperature";
    }

    @Override
    public List<String> forbidden() {
        return ColombianKitchen.FORBIDDEN;
    }
}
