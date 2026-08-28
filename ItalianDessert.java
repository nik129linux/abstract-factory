import java.util.List;

/** Concrete product: Italian dessert. */
public class ItalianDessert extends AbstractCourse implements Dessert {

    @Override
    public String tradition() {
        return ItalianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "cream, coffee or seasonal fruit; layered or in a cup, rests in the cold before serving";
    }

    @Override
    public List<String> forbidden() {
        return ItalianKitchen.FORBIDDEN;
    }
}
