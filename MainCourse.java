/** Producto abstracto 2 de 4. */
public interface MainCourse extends Course {

    @Override
    default String role() {
        return "Plato fuerte";
    }
}
