import java.util.List;

/** Concrete product: Japanese dessert. */
public class JapaneseDessert extends AbstractCourse implements Dessert {

    @Override
    public String tradition() {
        return JapaneseKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "barely sweet, glutinous rice textures, matcha or red bean, no oven and no dairy";
    }

    @Override
    public List<String> forbidden() {
        return JapaneseKitchen.FORBIDDEN;
    }
}
