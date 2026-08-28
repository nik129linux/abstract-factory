/**
 * LA FABRICA ABSTRACTA.
 *
 * Una cocina = una tradicion culinaria, y es la unica forma de conseguir un
 * plato. Por eso los cuatro platos que entrega siempre pegan entre si: no hay
 * manera de pedirle a la cocina japonesa un postre italiano, la firma no existe.
 *
 * Quien programa contra esta interfaz nunca escribe el nombre de una clase
 * concreta, y ese es el punto del patron.
 */
public interface Kitchen {

    /** Como se llama la familia. Es lo que el agente contesta al elegir. */
    String tradition();

    /** Una linea que describe la cocina, para meterla en el prompt del modelo. */
    String accent();

    Starter createStarter();

    MainCourse createMainCourse();

    Dessert createDessert();

    Drink createDrink();
}
