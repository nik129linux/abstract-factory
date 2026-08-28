import java.util.List;

/** Concrete product: Colombian drink. */
public class ColombianDrink extends AbstractCourse implements Drink {

    @Override
    public String tradition() {
        return ColombianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "fruit juice with water, an aguapanela or coffee from the farm; served in a jug to share";
    }

    @Override
    public List<String> forbidden() {
        return ColombianKitchen.FORBIDDEN;
    }
}
