import java.util.List;
import java.util.Locale;

/**
 * THE WAITER. The only one that talks to the client.
 *
 * And it does not chat: on every turn it picks ONE action out of a closed set,
 * and the program runs it. That is the difference from a chatbot -- the model
 * does not write text that gets displayed and that is that, it decides what the
 * system does. If it answers with anything that is not one of the four actions,
 * the turn is rejected.
 *
 * It does not quote prices either: the pantry adds those up.
 */
public class Waiter {

    /** The only four things the waiter can do. */
    public enum Action { ASK, BUILD, ADJUST, REFUSE }

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
                ROLE: waiter
                You are the waiter of a catering service. Your job is NOT to chat:
                it is to put together an order and send it to be cooked. Never quote prices.

                Kitchens available: %s
                Order so far: %s
                Current combo: %s

                Conversation:
                %s
                client: %s

                YOU choose the kitchen. Never ask the client which cuisine they want:
                decide it yourself from the occasion and what they said.

                Pick ONE action:
                ASK     only when the diners or the budget per person are still unknown
                BUILD   you know how many and how much: choose the kitchen and cook it
                ADJUST  there is already a combo and the client wants something changed
                REFUSE  only when the client asks for something that is not catering at all

                Answer in exactly these lines, nothing else:
                ACTION: <ASK|BUILD|ADJUST|REFUSE>
                SAY: <one single line for the client, no prices>
                DINERS: <number or ->
                BUDGET: <pesos per person, digits only, or ->
                OCCASION: <short text or ->
                RESTRICTIONS: <short text or ->
                KITCHEN: <%s or ->
                CHANGE: <starter|main|dessert|drink|all or ->
                """.formatted(
                        String.join(", ", Kitchens.traditions()),
                        order.describe(),
                        combo == null ? "none yet"
                                : combo.kitchen().tradition() + " kitchen, "
                                  + combo.size() + " courses, $" + combo.costPerPerson() + " per person",
                        history.isEmpty() ? "(just starting)" : String.join("\n", history),
                        said,
                        String.join("|", Kitchens.traditions()));

        String reply = agent.ask(prompt);

        Action action = parse(Reply.line(reply, "ACTION"));
        String say = Reply.line(reply, "SAY");
        if (say.isEmpty()) {
            say = "Tell me a bit more about the event.";
        }

        return new Decision(action, say,
                Reply.number(Reply.line(reply, "DINERS")),
                Reply.number(Reply.line(reply, "BUDGET")),
                Reply.line(reply, "OCCASION"),
                Reply.line(reply, "RESTRICTIONS"),
                Reply.line(reply, "KITCHEN").toLowerCase(Locale.ROOT),
                Reply.line(reply, "CHANGE").toLowerCase(Locale.ROOT));
    }

    /** Outside the closed set there is no valid action: it just asks. */
    private static Action parse(String written) {
        String clean = written.toUpperCase(Locale.ROOT);
        for (Action action : Action.values()) {
            if (clean.contains(action.name())) {
                return action;
            }
        }
        return Action.ASK;
    }
}
