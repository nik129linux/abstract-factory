/**
 * THE ABSTRACT FACTORY.
 *
 * One kitchen = one culinary tradition, and it is the only way to get a dish.
 * That is why the four dishes it hands out always belong together: there is no
 * way to ask the japanese kitchen for an italian dessert, the signature does
 * not exist.
 *
 * Code written against this interface never names a concrete class, and that is
 * the point of the pattern.
 */
public interface Kitchen {

    /** The family's name. It is what the agent answers when it picks one. */
    String tradition();

    /** One line describing the kitchen, to drop into the model's prompt. */
    String accent();

    Starter createStarter();

    MainCourse createMainCourse();

    Dessert createDessert();

    Drink createDrink();
}
