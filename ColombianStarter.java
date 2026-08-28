import java.util.List;

/** Concrete product: Colombian starter. */
public class ColombianStarter extends AbstractCourse implements Starter {

    @Override
    public String tradition() {
        return ColombianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "something made of corn or plantain, or a short broth; served hot, with the chilli on the side, never on top";
    }

    @Override
    public List<String> forbidden() {
        return ColombianKitchen.FORBIDDEN;
    }
}
