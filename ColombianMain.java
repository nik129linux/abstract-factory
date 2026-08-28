import java.util.List;

/** Concrete product: Colombian main course. */
public class ColombianMain extends AbstractCourse implements MainCourse {

    @Override
    public String tradition() {
        return ColombianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "a sofrito base, a starch and a protein on the same plate; long cooking, served in a deep plate or a platter";
    }

    @Override
    public List<String> forbidden() {
        return ColombianKitchen.FORBIDDEN;
    }
}
