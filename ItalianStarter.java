import java.util.List;

/** Producto concreto: entrada italiana. */
public class ItalianStarter extends AbstractCourse implements Starter {

    @Override
    public String tradition() {
        return ItalianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "antipasto de tres o cuatro ingredientes, se come con la mano o con pan, "
             + "aceite de oliva de por medio, nada de salsa caliente";
    }

    @Override
    public List<String> forbidden() {
        return ItalianKitchen.FORBIDDEN;
    }
}
