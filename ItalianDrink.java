import java.util.List;

/** Concrete product: Italian drink. */
public class ItalianDrink extends AbstractCourse implements Drink {

    @Override
    public String tradition() {
        return ItalianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "wine from an italian region, a bitter aperitivo or a short coffee; name the region, not the brand";
    }

    @Override
    public List<String> forbidden() {
        return ItalianKitchen.FORBIDDEN;
    }
}
