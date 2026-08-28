import java.util.List;

/** Concrete product: Japanese starter. */
public class JapaneseStarter extends AbstractCourse implements Starter {

    @Override
    public String tradition() {
        return JapaneseKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "a small plate to open the appetite, dashi or rice vinegar as a base, served cold or warm, nothing deep fried";
    }

    @Override
    public List<String> forbidden() {
        return JapaneseKitchen.FORBIDDEN;
    }
}
