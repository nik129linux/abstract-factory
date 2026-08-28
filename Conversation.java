import java.util.ArrayList;
import java.util.List;

/**
 * El estado de la conversacion del lado del servidor: los turnos, el pedido a
 * medio llenar y el combo vigente.
 *
 * Es tambien quien ejecuta la accion que eligio el mesero. El mesero decide,
 * esto hace -- y lo que hace es siempre construir o rehacer objetos con la
 * fabrica, nunca escribir texto.
 */
public class Conversation {

    private static final int BUDGET_ROUNDS = 3;

    private final Waiter waiter;
    private final Chef chef;

    private final List<String> history = new ArrayList<>();
    private Order order = Order.empty();
    private Combo combo;
    private String lastAction = "";
    private String lastSay = "";

    public Conversation(Agent agent) {
        this.waiter = new Waiter(agent);
        this.chef = new Chef(agent);
    }

    public Order order() {
        return order;
    }

    public Combo combo() {
        return combo;
    }

    public String lastAction() {
        return lastAction;
    }

    public String lastSay() {
        return lastSay;
    }

    public List<String> log() {
        return chef.log();
    }

    /** Un turno del cliente. Devuelve nada: el estado queda en el objeto. */
    public void say(String said) {
        chef.clearLog();
        Waiter.Decision decision = waiter.listen(said, order, combo, history);
        order = order.merge(decision.diners(), decision.budget(),
                            decision.occasion(), decision.restrictions());

        Waiter.Action action = decision.action();
        String say = decision.say();

        // El modelo puede querer construir sin tener los datos. El programa manda.
        if (action == Waiter.Action.CONSTRUIR && !order.isComplete()) {
            action = Waiter.Action.PREGUNTAR;
            say = "Antes de armarlo necesito " + order.missing() + ".";
        }
        if (action == Waiter.Action.AJUSTAR && combo == null) {
            action = order.isComplete() ? Waiter.Action.CONSTRUIR : Waiter.Action.PREGUNTAR;
        }

        switch (action) {
            case CONSTRUIR -> build(decision, said);
            case AJUSTAR -> adjust(decision, said);
            default -> { }
        }

        lastAction = action.name();
        lastSay = say;
        history.add("cliente: " + said);
        history.add("mesero: " + say);
        if (history.size() > 12) {
            history.subList(0, 2).clear();
        }
    }

    /** El mesero nombro una cocina; la fabrica hace el resto. */
    private void build(Waiter.Decision decision, String said) {
        Kitchen kitchen;
        try {
            kitchen = Kitchens.byTradition(decision.kitchen());
        } catch (IllegalArgumentException e) {
            kitchen = Kitchens.byTradition(decision.say() + " " + said);
        }
        combo = new Combo(kitchen);
        chef.cook(combo, order, said);
        fitBudget(said);
    }

    private void adjust(Waiter.Decision decision, String said) {
        for (int n : coursesToRedo(decision.change())) {
            combo.redo(n);
            chef.cookOne(combo, n, order, said);
        }
        fitBudget(said);
    }

    /**
     * La restriccion global: el combo entero tiene que caber en el presupuesto.
     * Si no cabe, el chef vuelve sobre el plato mas caro. Esto es lo que hace
     * que el agente no sea lineal -- vuelve atras sobre trabajo ya hecho.
     */
    private void fitBudget(String said) {
        for (int round = 0; round < BUDGET_ROUNDS; round++) {
            if (order.budget() <= 0 || combo.costPerPerson() <= order.budget()) {
                return;
            }
            int worst = combo.priciest();
            int over = combo.costPerPerson() - order.budget();
            // No se le puede pedir a un solo plato que absorba todo el exceso: se
            // le pide bajar a una cuota sensata del combo y se vuelve a mirar.
            int cap = Math.max(order.budget() * 3 / 10, combo.course(worst).cost() - over);
            combo.redo(worst);
            chef.cookOne(combo, worst, order,
                    said + " (el combo se paso del tope, este plato tiene que bajar)", cap);
        }
    }

    private List<Integer> coursesToRedo(String change) {
        if (change.contains("todo") || change.isEmpty()) {
            return List.of(1, 2, 3, 4);
        }
        List<Integer> which = new ArrayList<>();
        if (change.contains("entrada")) {
            which.add(1);
        }
        if (change.contains("fuerte") || change.contains("plato")) {
            which.add(2);
        }
        if (change.contains("postre")) {
            which.add(3);
        }
        if (change.contains("bebida")) {
            which.add(4);
        }
        return which.isEmpty() ? List.of(1, 2, 3, 4) : which;
    }
}
