import java.util.ArrayList;
import java.util.List;

/**
 * THE CHEF. Talks to nobody: it takes the order and cooks.
 *
 * The loop lives here, and the loop is what separates an agent from a template
 * filler: it proposes a dish, the program VALIDATES it against the pantry, the
 * family's bans, repeated proteins and a price ceiling, and when it does not
 * pass the chef hands back the written reason so the model can correct itself.
 * That reason is literally the same string that goes into the retry's prompt.
 *
 * After MAX_TRIES it gives up and keeps the cheapest attempt. An agent that
 * cannot give up hangs the demo.
 */
public class Chef {

    public static final int MAX_TRIES = 3;

    private final Agent agent;
    private final List<String> log = new ArrayList<>();

    public Chef(Agent agent) {
        this.agent = agent;
    }

    /** Everything that happened while cooking, for the interface to show. */
    public List<String> log() {
        return List.copyOf(log);
    }

    public void clearLog() {
        log.clear();
    }

    /** Cooks whichever courses of the combo are still empty. */
    public void cook(Combo combo, Order order, String extra) {
        for (int n = 1; n <= combo.size(); n++) {
            if (!combo.course(n).isFilled()) {
                cookOne(combo, n, order, extra);
            }
        }
    }

    /** One course, with its retries. */
    public void cookOne(Combo combo, int n, Order order, String extra) {
        cookOne(combo, n, order, extra, 0);
    }

    /**
     * cap is THIS course's ceiling in COP. Without a ceiling the chef accepts a
     * dish that fits on its own but blows up the combo, which is exactly what
     * used to happen.
     */
    public void cookOne(Combo combo, int n, Order order, String extra, int cap) {
        Course course = combo.course(n);
        String feedback = "";

        // If none of them pass, keep the cheapest and not the last one: the last
        // attempt can be worse than the first and the combo would get worse.
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
        log.add("gave up|" + course.role() + "|" + MAX_TRIES + "|keeping the cheapest: "
                + course.name() + " ($" + course.cost() + ")");
    }

    /** The validation. The model cannot do any of these five on its own. */
    private String check(Course course, Combo combo, Order order, int cap) {
        if (!course.isFilled()) {
            return "you did not return the format: the NAME line is missing";
        }
        if (course.ingredients().isEmpty()) {
            return "you did not return the format: the INGREDIENTS line is missing";
        }
        if (!course.missing().isEmpty()) {
            return "not in the warehouse: " + String.join(", ", course.missing())
                 + ". Use only ingredients from the pantry list";
        }
        if (!course.violations().isEmpty()) {
            return "that is not " + course.tradition() + " cooking: "
                 + String.join(", ", course.violations());
        }
        int roof = cap > 0 ? cap : order.budget();
        if (roof > 0 && course.cost() > roof) {
            return "that dish costs $" + course.cost() + " and cannot go over $" + roof
                 + ". Pick cheaper ingredients from the pantry";
        }
        String repeated = repeats(course, combo);
        if (!repeated.isEmpty()) {
            return "you already used " + repeated + " in another dish of the combo, change it";
        }
        return "";
    }

    /** A combo that repeats the protein in two dishes is not a combo. */
    private String repeats(Course course, Combo combo) {
        for (Course other : combo.courses()) {
            if (other == course || !other.isFilled()) {
                continue;
            }
            for (String item : course.ingredients()) {
                String key = Pantry.get().match(item);
                if (key == null || Pantry.get().price(key) < 5000) {
                    continue;   // only repeating the expensive things matters: the protein
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
                ROLE: dish
                You are the chef of a %s catering service.
                Order: %s%s

                Your part of the combo is: %s.
                House rules: %s
                Never use: %s

                You may only use ingredients from this pantry, at these prices:
                %s
                %s
                Answer in exactly three lines, no headings and no commentary:
                NAME: <name of the dish>
                INGREDIENTS: <ingredient> | <ingredient> | <ingredient>
                STEPS: <step> | <step> | <step>
                """.formatted(
                        course.tradition(),
                        order.describe(),
                        extra.isEmpty() ? "" : "\nThe client also asked for: " + extra,
                        course.role(),
                        course.rules(),
                        String.join(", ", course.forbidden()),
                        Pantry.get().priceList(course.tradition()),
                        feedback.isEmpty() ? ""
                                : "\nYour previous attempt was REJECTED: " + feedback + "\nFix it.\n");

        String reply = agent.ask(prompt);
        course.fill(Reply.line(reply, "NAME"),
                    Reply.split(Reply.line(reply, "INGREDIENTS")),
                    Reply.split(Reply.line(reply, "STEPS")));
    }
}
