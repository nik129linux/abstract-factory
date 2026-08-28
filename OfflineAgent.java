import java.util.List;
import java.util.Locale;

/**
 * El mismo agente sin red.
 *
 * No es parte del taller: es el plan B para sustentar. gemma4:31b-cloud pasa por
 * Ollama pero el modelo corre afuera, asi que sin internet en el salon la
 * interfaz quedaria muerta en la mitad de la demo. Con esto se cambia con un
 * flag y el flujo se ve igual.
 *
 * Que exista dos veces el agente tambien prueba algo: ni el Menu ni la fabrica
 * saben de donde salen los platos, hablan contra la interfaz Agent.
 */
public class OfflineAgent implements Agent {

    private String lastPrompt = "";
    private String lastReply = "";

    @Override
    public String name() {
        return "offline (sin modelo)";
    }

    @Override
    public String chooseTradition(Order order, List<String> available) {
        lastPrompt = order.describe();
        String text = order.describe().toLowerCase(Locale.ROOT);
        for (String tradition : available) {
            if (text.contains(tradition)) {
                lastReply = tradition;
                return tradition;
            }
        }
        lastReply = available.get(Math.floorMod(text.hashCode(), available.size()));
        return lastReply;
    }

    @Override
    public void fill(Course course, Order order) {
        lastPrompt = course.tradition() + " / " + course.role();
        String[] dish = canned(course.tradition(), course.role());
        lastReply = "NOMBRE: " + dish[0] + "\nINGREDIENTES: " + dish[1] + "\nPASOS: " + dish[2];
        course.fill(dish[0], List.of(dish[1].split(" \\| ")), List.of(dish[2].split(" \\| ")));
    }

    @Override
    public String lastPrompt() {
        return lastPrompt;
    }

    @Override
    public String lastReply() {
        return lastReply;
    }

    private static String[] canned(String tradition, String role) {
        return switch (tradition + "/" + role) {
            case "japonesa/Entrada" -> new String[]{"Sunomono de pepino",
                    "pepino | vinagre de arroz | alga wakame | ajonjoli",
                    "cortar el pepino fino | salar y escurrir | mezclar con el vinagre | enfriar"};
            case "japonesa/Plato fuerte" -> new String[]{"Donburi de salmon",
                    "arroz japones | salmon | salsa de soya | cebollin",
                    "cocer el arroz | sellar el salmon | glasear con soya | montar en cuenco"};
            case "japonesa/Postre" -> new String[]{"Mochi de matcha",
                    "harina de arroz glutinoso | matcha | azucar | fecula de maiz",
                    "mezclar y cocer al vapor | amasar en frio | porcionar | pasar por fecula"};
            case "japonesa/Bebida" -> new String[]{"Te de cebada tostada",
                    "cebada tostada | agua",
                    "tostar la cebada | infusionar diez minutos | colar | servir frio"};
            case "italiana/Entrada" -> new String[]{"Bruschetta de tomate",
                    "pan rustico | tomate maduro | albahaca | aceite de oliva",
                    "tostar el pan | picar el tomate | frotar ajo en el pan | montar y aceitar"};
            case "italiana/Plato fuerte" -> new String[]{"Cacio e pepe",
                    "espagueti | pecorino | pimienta negra | agua de coccion",
                    "cocer la pasta | tostar la pimienta | emulsionar el queso | ligar fuera del fuego"};
            case "italiana/Postre" -> new String[]{"Panna cotta de vainilla",
                    "crema | azucar | vainilla | gelatina",
                    "calentar la crema | disolver la gelatina | moldear | enfriar cuatro horas"};
            case "italiana/Bebida" -> new String[]{"Tinto del Piamonte",
                    "vino tinto de nebbiolo",
                    "abrir treinta minutos antes | servir a dieciseis grados"};
            case "colombiana/Entrada" -> new String[]{"Arepa de choclo con quesito",
                    "maiz tierno | quesito | mantequilla | sal",
                    "moler el maiz | armar las arepas | asar en budare | abrir y rellenar"};
            case "colombiana/Plato fuerte" -> new String[]{"Sudado de pollo",
                    "pollo | papa | guiso de cebolla y tomate | cilantro",
                    "hacer el guiso | sellar el pollo | agregar papa y agua | cocinar a fuego bajo"};
            case "colombiana/Postre" -> new String[]{"Brevas con arequipe",
                    "brevas | panela | arequipe",
                    "cocer las brevas en agua de panela | dejar reposar un dia | rellenar con arequipe"};
            case "colombiana/Bebida" -> new String[]{"Limonada de panela",
                    "limon | panela | agua",
                    "derretir la panela | exprimir el limon | mezclar | servir en jarra con hielo"};
            default -> new String[]{"plato de la casa", "ingrediente", "paso"};
        };
    }
}
