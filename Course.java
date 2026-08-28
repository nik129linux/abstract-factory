import java.util.List;

/**
 * Lo que todo plato de la carta sabe responder, sea de la cocina que sea.
 *
 * Un plato nace vacio. La fabrica decide QUE TIPO de plato es -- entrada
 * japonesa, postre italiano -- y el agente decide CUAL plato es. Ese corte es
 * todo el taller: la familia la fija el codigo, el contenido lo fija el modelo.
 */
public interface Course {

    /** Entrada, plato fuerte, postre o bebida. Lo pone el tipo de producto. */
    String role();

    /** Japonesa, italiana o colombiana. Lo pone la familia. */
    String tradition();

    /** Lo que el agente tiene que respetar al llenar este plato. */
    String rules();

    /** Palabras que significan que el agente se salio de la familia. */
    List<String> forbidden();

    /** Lo unico que el agente puede tocar. */
    void fill(String name, List<String> ingredients, List<String> steps);

    boolean isFilled();

    String name();

    List<String> ingredients();

    List<String> steps();

    /** Las prohibidas que el agente si uso. Vacio = la familia aguanto. */
    List<String> violations();
}
