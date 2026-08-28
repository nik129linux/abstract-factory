import java.util.List;

/** Concrete product: Italian main course. */
public class ItalianMain extends AbstractCourse implements MainCourse {

    @Override
    public String tradition() {
        return ItalianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "pasta, risotto or oven; the sauce is made from what is already on the plate, cheese grated at the end";
    }

    @Override
    public List<String> forbidden() {
        return ItalianKitchen.FORBIDDEN;
    }
}
