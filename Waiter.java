import java.util.List;
import java.util.Locale;

/**
 * EL MESERO. El unico que habla con el cliente.
 *
 * Y no conversa: en cada turno elige UNA accion de un conjunto cerrado, y el
 * programa la ejecuta. Ahi esta la diferencia con un chatbot -- el modelo no
 * escribe texto que se muestra y ya, decide que hace el sistema. Si contesta
 * algo que no es una de las cuatro acciones, el turno se rechaza.
 *
 * Tampoco dice precios: eso lo suma la despensa.
 */
public class Waiter {

    /** Las unicas cuatro cosas que el mesero puede hacer. */
    public enum Action { PREGUNTAR, CONSTRUIR, AJUSTAR, RECHAZAR }

    public record Decision(Action action, String say, int diners, int budget,
                           String occasion, String restrictions,
                           String kitchen, String change) {
    }

    private final Agent agent;

    public Waiter(Agent agent) {
        this.agent = agent;
    }

    public Decision listen(String said, Order order, Combo combo, List<String> history) {
        String prompt = """
                ROL: mesero
                Sos el mesero de un servicio de catering. Tu trabajo NO es conversar:
                es armar un pedido y mandarlo a cocinar. Nunca digas precios.

                Cocinas disponibles: %s
                Pedido hasta ahora: %s
                Combo actual: %s

                Conversacion:
                %s
                cliente: %s

                Elegi UNA accion:
                PREGUNTAR  falta un dato del pedido (comensales o presupuesto por persona)
                CONSTRUIR  ya sabes cuantos son y cuanto gastan: elegi la cocina y mandalo a cocinar
                AJUSTAR    ya hay un combo y el cliente quiere cambiar algo
                RECHAZAR   el cliente pide algo que no es armar un combo de catering

                Responde exactamente en estas lineas, sin nada mas:
                ACCION: <PREGUNTAR|CONSTRUIR|AJUSTAR|RECHAZAR>
                DECIR: <una sola linea para el cliente, en espanol, sin precios>
                COMENSALES: <numero o ->
                PRESUPUESTO: <pesos por persona, solo el numero, o ->
                OCASION: <texto corto o ->
                RESTRICCIONES: <texto corto o ->
                COCINA: <%s o ->
                CAMBIAR: <entrada|fuerte|postre|bebida|todo o ->
                """.formatted(
                        String.join(", ", Kitchens.traditions()),
                        order.describe(),
                        combo == null ? "ninguno todavia"
                                : "cocina " + combo.kitchen().tradition() + ", "
                                  + combo.size() + " platos, $" + combo.costPerPerson() + " por persona",
                        history.isEmpty() ? "(recien empieza)" : String.join("\n", history),
                        said,
                        String.join("|", Kitchens.traditions()));

        String reply = agent.ask(prompt);

        Action action = parse(Reply.line(reply, "ACCION"));
        String say = Reply.line(reply, "DECIR");
        if (say.isEmpty()) {
            say = "Contame un poco mas del evento.";
        }

        return new Decision(action, say,
                Reply.number(Reply.line(reply, "COMENSALES")),
                Reply.number(Reply.line(reply, "PRESUPUESTO")),
                Reply.line(reply, "OCASION"),
                Reply.line(reply, "RESTRICCIONES"),
                Reply.line(reply, "COCINA").toLowerCase(Locale.ROOT),
                Reply.line(reply, "CAMBIAR").toLowerCase(Locale.ROOT));
    }

    /** Fuera del conjunto cerrado no hay accion valida: se pregunta y ya. */
    private static Action parse(String written) {
        String clean = written.toUpperCase(Locale.ROOT);
        for (Action action : Action.values()) {
            if (clean.contains(action.name())) {
                return action;
            }
        }
        return Action.PREGUNTAR;
    }
}
