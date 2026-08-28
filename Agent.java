/**
 * La boca del modelo, y nada mas. Recibe un prompt y devuelve texto.
 *
 * Los que deciden son los dos roles que hay encima: Waiter (el mesero, el unico
 * que habla con el cliente) y Chef (que no habla con nadie y construye contra la
 * fabrica y la despensa). Que los dos usen la misma interfaz es lo que permite
 * cambiar el modelo por el agente offline sin que ninguno de los dos se entere.
 *
 * La primera linea de todo prompt dice "ROL: ..." para que el agente offline
 * sepa que le estan pidiendo sin tener que entender el texto.
 */
public interface Agent {

    String name();

    String ask(String prompt);

    String lastPrompt();

    String lastReply();
}
