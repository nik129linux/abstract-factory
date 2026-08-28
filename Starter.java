/**
 * Abstract product 1 of 4. The role does not change between kitchens: a starter
 * is a starter in Tokyo and in Medellin, which is why it lives here and not in
 * the concrete classes.
 */
public interface Starter extends Course {

    @Override
    default String role() {
        return "Starter";
    }
}
