import java.util.List;

/** Concrete product: Italian starter. */
public class ItalianStarter extends AbstractCourse implements Starter {

    @Override
    public String tradition() {
        return ItalianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "an antipasto of three or four ingredients, eaten by hand or with bread, olive oil involved, no hot sauce";
    }

    @Override
    public List<String> forbidden() {
        return ItalianKitchen.FORBIDDEN;
    }
}
