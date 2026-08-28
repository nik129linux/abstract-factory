import java.util.ArrayList;
import java.util.List;

/**
 * EL CHEF. No habla con nadie: recibe el pedido y cocina.
 *
 * Aca esta el bucle, que es lo que separa a un agente de un rellenador de
 * plantillas: propone un plato, el programa lo VALIDA contra la despensa y
 * contra las prohibiciones de la familia, y si no pasa le devuelve el motivo
 * escrito para que corrija. El motivo del rechazo es literalmente el mismo
 * string que entra al prompt del reintento.
 *
 * Despues de MAX_TRIES se rinde y deja el ultimo intento marcado. Un agente que
 * no sabe rendirse cuelga la demo.
 */
public class Chef {

    public static final int MAX_TRIES = 3;

    private final Agent agent;
    private final List<String> log = new ArrayList<>();

    public Chef(Agent agent) {
        this.agent = agent;
    }

    /** Todo lo que paso mientras cocinaba, para que la interfaz lo muestre. */
    public List<String> log() {
        return List.copyOf(log);
    }

    public void clearLog() {
        log.clear();
    }

    /** Cocina los platos que falten del combo. */
    public void cook(Combo combo, Order order, String extra) {
        for (int n = 1; n <= combo.size(); n++) {
            if (!combo.course(n).isFilled()) {
                cookOne(combo, n, order, extra);
            }
        }
    }

    /** Un plato, con sus reintentos. */
    public void cookOne(Combo combo, int n, Order order, String extra) {
        cookOne(combo, n, order, extra, 0);
    }

    /**
     * cap es el techo de ESTE plato en COP. Sin techo el chef acepta un plato
     * que cabe solo pero revienta el combo, que es justo lo que pasaba.
     */
    public void cookOne(Combo combo, int n, Order order, String extra, int cap) {
        Course course = combo.course(n);
        String feedback = "";

        // Si ninguno pasa hay que quedarse con el mas barato, no con el ultimo:
        // el ultimo intento puede ser peor que el primero y el combo empeoraria.
        String bestName = "";
        List<String> bestIngredients = List.of();
        List<String> bestSteps = List.of();
        int bestCost = Integer.MAX_VALUE;

        for (int attempt = 1; attempt <= MAX_TRIES; attempt++) {
            propose(course, combo, order, extra, feedback);

            String problem = check(course, combo, order, cap);
            if (problem.isEmpty()) {
                log.add("ok|" + course.role() + "|" + attempt + "|" + course.name()
                        + " ($" + course.cost() + ")");
                return;
            }

            if (course.isFilled() && course.cost() > 0 && course.cost() < bestCost) {
                bestCost = course.cost();
                bestName = course.name();
                bestIngredients = course.ingredients();
                bestSteps = course.steps();
            }

            log.add("no|" + course.role() + "|" + attempt + "|" + course.name() + " -- " + problem);
            feedback = problem;
        }

        if (bestCost < Integer.MAX_VALUE && bestCost < course.cost()) {
            course.fill(bestName, bestIngredients, bestSteps);
        }
        log.add("rendido|" + course.role() + "|" + MAX_TRIES + "|se queda el mas barato: "
                + course.name() + " ($" + course.cost() + ")");
    }

    /** La validacion. Ninguna de las tres la puede hacer el modelo por si solo. */
    private String check(Course course, Combo combo, Order order, int cap) {
        if (!course.isFilled()) {
            return "no devolviste el formato: falta la linea NOMBRE";
        }
        if (course.ingredients().isEmpty()) {
            return "no devolviste el formato: falta la linea INGREDIENTES";
        }
        if (!course.missing().isEmpty()) {
            return "no hay en la bodega: " + String.join(", ", course.missing())
                 + ". Usa solo ingredientes de la lista de la despensa";
        }
        if (!course.violations().isEmpty()) {
            return "eso no es cocina " + course.tradition() + ": "
                 + String.join(", ", course.violations());
        }
        int roof = cap > 0 ? cap : order.budget();
        if (roof > 0 && course.cost() > roof) {
            return "ese plato cuesta $" + course.cost() + " y no puede pasar de $" + roof
                 + ". Elegi ingredientes mas baratos de la despensa";
        }
        String repeated = repeats(course, combo);
        if (!repeated.isEmpty()) {
            return "ya usaste " + repeated + " en otro plato del combo, cambialo";
        }
        return "";
    }

    /** Un combo que repite la proteina en dos platos no es un combo. */
    private String repeats(Course course, Combo combo) {
        for (Course other : combo.courses()) {
            if (other == course || !other.isFilled()) {
                continue;
            }
            for (String item : course.ingredients()) {
                String key = Pantry.get().match(item);
                if (key == null || Pantry.get().price(key) < 5000) {
                    continue;   // solo importa repetir lo caro: la proteina
                }
                for (String taken : other.ingredients()) {
                    if (key.equals(Pantry.get().match(taken))) {
                        return key;
                    }
                }
            }
        }
        return "";
    }

    private void propose(Course course, Combo combo, Order order, String extra, String feedback) {
        String prompt = """
                ROL: plato
                Sos el chef de un servicio de catering de cocina %s.
                Pedido: %s%s

                Te toca esta parte del combo: %s.
                Reglas de la casa: %s
                Nunca uses: %s

                Solo podes usar ingredientes de esta despensa, con estos precios:
                %s
                %s
                Responde exactamente en tres lineas, sin titulos ni comentarios:
                NOMBRE: <nombre del plato>
                INGREDIENTES: <ingrediente> | <ingrediente> | <ingrediente>
                PASOS: <paso> | <paso> | <paso>
                """.formatted(
                        course.tradition(),
                        order.describe(),
                        extra.isEmpty() ? "" : "\nEl cliente pidio ademas: " + extra,
                        course.role(),
                        course.rules(),
                        String.join(", ", course.forbidden()),
                        Pantry.get().priceList(course.tradition()),
                        feedback.isEmpty() ? ""
                                : "\nTu intento anterior fue RECHAZADO: " + feedback + "\nCorregilo.\n");

        String reply = agent.ask(prompt);
        course.fill(Reply.line(reply, "NOMBRE"),
                    Reply.split(Reply.line(reply, "INGREDIENTES")),
                    Reply.split(Reply.line(reply, "PASOS")));
    }
}
