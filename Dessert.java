/** Producto abstracto 3 de 4. */
public interface Dessert extends Course {

    @Override
    default String role() {
        return "Postre";
    }
}
