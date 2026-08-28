/**
 * Producto abstracto 1 de 4. El rol no cambia entre cocinas: una entrada es una
 * entrada en Tokio y en Medellin, y por eso vive aca y no en las concretas.
 */
public interface Starter extends Course {

    @Override
    default String role() {
        return "Entrada";
    }
}
