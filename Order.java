/**
 * El pedido que entra por la interfaz. Es lo unico que escribe el humano en
 * todo el flujo: de aca en adelante decide el agente.
 *
 * Es un record porque no tiene comportamiento propio -- solo carga datos y se
 * describe a si mismo para el prompt.
 */
public record Order(int diners, String restrictions, String occasion, String notes) {

    public static Order of(String diners, String restrictions, String occasion, String notes) {
        int n;
        try {
            n = Integer.parseInt(diners.trim());
        } catch (RuntimeException e) {
            n = 2;
        }
        return new Order(Math.max(1, n), text(restrictions), text(occasion), text(notes));
    }

    private static String text(String value) {
        return value == null ? "" : value.trim();
    }

    /** Como se le cuenta el pedido al modelo. Una linea, sin adornos. */
    public String describe() {
        StringBuilder sb = new StringBuilder(diners + " comensales");
        if (!occasion.isEmpty()) {
            sb.append(", ocasion: ").append(occasion);
        }
        if (!restrictions.isEmpty()) {
            sb.append(", restricciones: ").append(restrictions);
        }
        if (!notes.isEmpty()) {
            sb.append(", nota del cliente: ").append(notes);
        }
        return sb.toString();
    }
}
