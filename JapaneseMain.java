import java.util.List;

/** Producto concreto: plato fuerte japones. */
public class JapaneseMain extends AbstractCourse implements MainCourse {

    @Override
    public String tradition() {
        return JapaneseKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "arroz o fideo como base, proteina a la parrilla o al vapor, "
             + "salsa a base de soya o miso, se monta en cuenco";
    }

    @Override
    public List<String> forbidden() {
        return JapaneseKitchen.FORBIDDEN;
    }
}
