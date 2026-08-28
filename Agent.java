import java.util.List;

/**
 * EL AGENTE. La pieza que reemplaza al humano.
 *
 * Hace las dos cosas que antes hacia una persona: elegir la tradicion que le
 * sirve al pedido, y decidir el plato concreto dentro de las reglas que el
 * producto de la fabrica ya trae puestas.
 *
 * Lo que NO hace, y es deliberado: no elige la clase. Contesta una palabra y
 * Kitchens.byTradition la traduce a una fabrica. Si el agente delirara y
 * contestara "peruana", el programa falla fuerte en vez de armar un menu roto.
 *
 * Tampoco conversa. No hay historial, no hay turnos, no hay usuario del otro
 * lado esperando una respuesta: se le pide una decision y devuelve una decision.
 */
public interface Agent {

    /** Como se presenta en la interfaz. */
    String name();

    /** Paso 1: mira el pedido y contesta con una de las tradiciones. */
    String chooseTradition(Order order, List<String> available);

    /** Paso 2: llena un plato respetando course.rules(). */
    void fill(Course course, Order order);

    /** Lo ultimo que se le mando al modelo, tal cual. */
    String lastPrompt();

    /** Lo ultimo que contesto, sin limpiar. La interfaz lo muestra. */
    String lastReply();
}
