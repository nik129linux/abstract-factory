/**
 * Producto abstracto 4 de 4. La bebida es la que mas duele si se mezcla: un
 * postre puede pasar desapercibido, un sake con bandeja paisa no.
 */
public interface Drink extends Course {

    @Override
    default String role() {
        return "Bebida";
    }
}
