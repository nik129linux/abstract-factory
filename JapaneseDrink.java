import java.util.List;

/** Concrete product: Japanese drink. */
public class JapaneseDrink extends AbstractCourse implements Drink {

    @Override
    public String tradition() {
        return JapaneseKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "green tea, roasted barley or sake; served in a cup or a tokkuri, no added sugar";
    }

    @Override
    public List<String> forbidden() {
        return JapaneseKitchen.FORBIDDEN;
    }
}
