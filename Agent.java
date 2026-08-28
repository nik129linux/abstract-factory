/**
 * The model's mouth, and nothing else. It takes a prompt and returns text.
 *
 * The ones that decide are the two roles above it: Waiter (the only one that
 * talks to the client) and Chef (which talks to nobody and builds against the
 * factory and the pantry). Both going through the same interface is what lets
 * the model be swapped for the offline agent without either of them noticing.
 *
 * Every prompt opens with a "ROLE: ..." line so the offline agent knows what is
 * being asked of it without having to understand the text.
 */
public interface Agent {

    String name();

    String ask(String prompt);

    String lastPrompt();

    String lastReply();
}
