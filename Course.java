import java.util.List;

/**
 * What every dish on the menu can answer, no matter the tradition or the role.
 *
 * A course is born empty. The factory decides WHAT KIND of dish it is -- a
 * japanese starter, an italian dessert -- and the agent decides WHICH dish it
 * is. That split is the whole assignment: the code fixes the family, the model
 * fills in the content.
 */
public interface Course {

    /** Starter, main, dessert or drink. Comes from the product type. */
    String role();

    /** Japanese, italian or colombian. Comes from the family. */
    String tradition();

    /** What the agent has to respect when it fills this course in. */
    String rules();

    /** Words that mean the agent walked out of the family. */
    List<String> forbidden();

    /** The only thing the agent is allowed to touch. */
    void fill(String name, List<String> ingredients, List<String> steps);

    boolean isFilled();

    String name();

    List<String> ingredients();

    List<String> steps();

    /** Forbidden words the agent actually used. Empty means the family held. */
    List<String> violations();

    /** What the agent named that the pantry does not stock. */
    List<String> missing();

    /** COP per person. Comes from adding up the pantry, never from the model. */
    int cost();
}
