import java.util.List;

/** Producto concreto: bebida colombiana. */
public class ColombianDrink extends AbstractCourse implements Drink {

    @Override
    public String tradition() {
        return ColombianKitchen.TRADITION;
    }

    @Override
    public String rules() {
        return "jugo de fruta con agua, una aguapanela o un cafe de la finca; "
             + "se sirve en jarra para compartir";
    }

    @Override
    public List<String> forbidden() {
        return ColombianKitchen.FORBIDDEN;
    }
}
