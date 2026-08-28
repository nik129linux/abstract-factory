/** Abstract product 2 of 4. */
public interface MainCourse extends Course {

    @Override
    default String role() {
        return "Main";
    }
}
