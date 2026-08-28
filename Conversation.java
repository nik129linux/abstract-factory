import java.util.ArrayList;
import java.util.List;

/**
 * The server-side state of the conversation: the turns, the half-filled order
 * and the combo currently on the table.
 *
 * It is also what runs the action the waiter picked. The waiter decides, this
 * does -- and what it does is always build or rebuild objects with the factory,
 * never write text.
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

    /** One turn from the client. Returns nothing: the state stays in the object. */
    public void say(String said) {
        chef.clearLog();
        Waiter.Decision decision = waiter.listen(said, order, combo, history);
        order = order.merge(decision.diners(), decision.budget(),
                            decision.occasion(), decision.restrictions());

        Waiter.Action action = decision.action();
        String say = decision.say();

        // The model may want to build without having the details. The program wins.
        if (action == Waiter.Action.BUILD && !order.isComplete()) {
            action = Waiter.Action.ASK;
            say = "Before I put it together I need " + order.missing() + ".";
        }
        if (action == Waiter.Action.ADJUST && combo == null) {
            action = order.isComplete() ? Waiter.Action.BUILD : Waiter.Action.ASK;
        }

        switch (action) {
            case BUILD -> build(decision, said);
            case ADJUST -> adjust(decision, said);
            default -> { }
        }

        lastAction = action.name();
        lastSay = say;
        history.add("client: " + said);
        history.add("waiter: " + say);
        if (history.size() > 12) {
            history.subList(0, 2).clear();
        }
    }

    /** The waiter named a kitchen; the factory does the rest. */
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
     * The global constraint: the whole combo has to fit the budget. When it does
     * not, the chef goes back over the priciest dish. This is what stops the
     * agent from being linear -- it revisits work it had already finished.
     */
    private void fitBudget(String said) {
        for (int round = 0; round < BUDGET_ROUNDS; round++) {
            if (order.budget() <= 0 || combo.costPerPerson() <= order.budget()) {
                return;
            }
            int worst = combo.priciest();
            int over = combo.costPerPerson() - order.budget();
            // One dish cannot be asked to absorb the whole overshoot: it is asked
            // down to a sensible share of the combo and then we look again.
            int cap = Math.max(order.budget() * 3 / 10, combo.course(worst).cost() - over);
            combo.redo(worst);
            chef.cookOne(combo, worst, order,
                    said + " (the combo went over the cap, this dish has to come down)", cap);
        }
    }

    private List<Integer> coursesToRedo(String change) {
        if (change.contains("all") || change.isEmpty()) {
            return List.of(1, 2, 3, 4);
        }
        List<Integer> which = new ArrayList<>();
        if (change.contains("starter")) {
            which.add(1);
        }
        if (change.contains("main")) {
            which.add(2);
        }
        if (change.contains("dessert")) {
            which.add(3);
        }
        if (change.contains("drink")) {
            which.add(4);
        }
        return which.isEmpty() ? List.of(1, 2, 3, 4) : which;
    }
}
