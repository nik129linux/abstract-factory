/** Abstract product 3 of 4. */
public interface Dessert extends Course {

    @Override
    default String role() {
        return "Dessert";
    }
}
