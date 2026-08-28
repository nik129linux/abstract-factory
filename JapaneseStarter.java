import java.util.List;

/** Producto concreto: entrada japonesa. */
public class JapaneseStarter extends AbstractCourse implements Starter {

    @Override
    public String tradition() {
        return JapaneseKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "porcion chica para abrir el apetito, base de dashi o vinagre de arroz, "
             + "se sirve fria o tibia, nada frito pesado";
    }

    @Override
    public List<String> forbidden() {
        return JapaneseKitchen.FORBIDDEN;
    }
}
