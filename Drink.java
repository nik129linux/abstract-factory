/**
 * Abstract product 4 of 4. The drink is the one that hurts most when the
 * families get mixed: a dessert can slip by, sake with a bandeja paisa cannot.
 */
public interface Drink extends Course {

    @Override
    default String role() {
        return "Drink";
    }
}
