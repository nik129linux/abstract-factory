/**
 * El pedido. Se llena de a pedazos durante la conversacion: el mesero saca lo
 * que puede de cada turno y pregunta por lo que falta.
 *
 * Es inmutable a proposito -- cada turno produce un pedido nuevo en vez de
 * pisar el anterior, asi que se puede mostrar como fue creciendo.
 */
public record Order(int diners, int budget, String occasion, String restrictions) {

    public static Order empty() {
        return new Order(0, 0, "", "");
    }

    /** Solo pisa lo que venga con valor: el turno 3 no borra lo del turno 1. */
    public Order merge(int newDiners, int newBudget, String newOccasion, String newRestrictions) {
        return new Order(
                newDiners > 0 ? newDiners : diners,
                newBudget > 0 ? newBudget : budget,
                newOccasion.isBlank() ? occasion : newOccasion.trim(),
                newRestrictions.isBlank() ? restrictions : newRestrictions.trim());
    }

    /** Lo minimo para poder cocinar y cotizar. */
    public boolean isComplete() {
        return diners > 0 && budget > 0;
    }

    public String missing() {
        if (diners <= 0 && budget <= 0) {
            return "cuantos son y cuanto quieren gastar por persona";
        }
        if (diners <= 0) {
            return "cuantos comensales son";
        }
        if (budget <= 0) {
            return "cuanto quieren gastar por persona";
        }
        return "";
    }

    /** Como se le cuenta el pedido al modelo. */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(diners > 0 ? diners + " comensales" : "comensales sin definir");
        if (budget > 0) {
            sb.append(", hasta $").append(budget).append(" por persona");
        }
        if (!occasion.isEmpty()) {
            sb.append(", ocasion: ").append(occasion);
        }
        if (!restrictions.isEmpty()) {
            sb.append(", restricciones: ").append(restrictions);
        }
        return sb.toString();
    }
}
