import java.util.List;

/** Concrete product: Japanese main course. */
public class JapaneseMain extends AbstractCourse implements MainCourse {

    @Override
    public String tradition() {
        return JapaneseKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "rice or noodles as the base, protein grilled or steamed, a soy or miso sauce, served in a bowl";
    }

    @Override
    public List<String> forbidden() {
        return JapaneseKitchen.FORBIDDEN;
    }
}
