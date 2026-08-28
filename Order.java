/**
 * The order. It gets filled in pieces during the conversation: the waiter pulls
 * whatever it can out of each turn and asks for what is still missing.
 *
 * Immutable on purpose -- every turn produces a new order instead of writing
 * over the previous one, so it can be shown as it grew.
 */
public record Order(int diners, int budget, String occasion, String restrictions) {

    public static Order empty() {
        return new Order(0, 0, "", "");
    }

    /** Only overwrites what arrives with a value: turn 3 does not erase turn 1. */
    public Order merge(int newDiners, int newBudget, String newOccasion, String newRestrictions) {
        return new Order(
                newDiners > 0 ? newDiners : diners,
                newBudget > 0 ? newBudget : budget,
                newOccasion.isBlank() ? occasion : newOccasion.trim(),
                newRestrictions.isBlank() ? restrictions : newRestrictions.trim());
    }

    /** The minimum needed to cook and to quote. */
    public boolean isComplete() {
        return diners > 0 && budget > 0;
    }

    public String missing() {
        if (diners <= 0 && budget <= 0) {
            return "how many people and how much they want to spend each";
        }
        if (diners <= 0) {
            return "how many diners";
        }
        if (budget <= 0) {
            return "how much they want to spend per person";
        }
        return "";
    }

    /** How the order is told to the model. */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(diners > 0 ? diners + " diners" : "diners not set yet");
        if (budget > 0) {
            sb.append(", up to $").append(budget).append(" per person");
        }
        if (!occasion.isEmpty()) {
            sb.append(", occasion: ").append(occasion);
        }
        if (!restrictions.isEmpty()) {
            sb.append(", restrictions: ").append(restrictions);
        }
        return sb.toString();
    }
}
