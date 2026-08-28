import java.util.List;

/** Producto concreto: postre japones. */
public class JapaneseDessert extends AbstractCourse implements Dessert {

    @Override
    public String tradition() {
        return JapaneseKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "poco dulce, texturas de arroz glutinoso, matcha o frijol rojo, "
             + "sin horno y sin lacteos";
    }

    @Override
    public List<String> forbidden() {
        return JapaneseKitchen.FORBIDDEN;
    }
}
