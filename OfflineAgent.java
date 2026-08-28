import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * El mismo sistema sin modelo.
 *
 * No es parte del taller: es el plan B para sustentar. gemma4:31b-cloud pasa por
 * ollama pero el modelo corre afuera, asi que sin internet en el salon la
 * interfaz quedaria muerta en la mitad de la demo.
 *
 * Contesta en los mismos formatos que el modelo. Se orienta por la primera
 * linea del prompt, que dice el rol: por eso todos los prompts empiezan con
 * "ROL:". Ni el mesero ni el chef se enteran del cambio.
 */
public class OfflineAgent implements Agent {

    private static final Pattern NUMBER = Pattern.compile("\\d[\\d.]*");

    private String lastPrompt = "";
    private String lastReply = "";

    @Override
    public String name() {
        return "offline (sin modelo)";
    }

    @Override
    public String ask(String prompt) {
        lastPrompt = prompt;
        lastReply = prompt.startsWith("ROL: mesero") ? asWaiter(prompt) : asChef(prompt);
        return lastReply;
    }

    @Override
    public String lastPrompt() {
        return lastPrompt;
    }

    @Override
    public String lastReply() {
        return lastReply;
    }

    // ------------------------------------------------------------------ rol 1

    private String asWaiter(String prompt) {
        String said = after(prompt, "cliente: ");
        String known = line(prompt, "Pedido hasta ahora:");
        boolean hasCombo = !line(prompt, "Combo actual:").contains("ninguno");

        int diners = 0;
        int budget = 0;
        Matcher m = NUMBER.matcher(said);
        while (m.find()) {
            int value = Integer.parseInt(m.group().replace(".", ""));
            if (value >= 1000) {
                budget = value;
            } else if (value >= 1 && value <= 100) {
                if (said.toLowerCase(Locale.ROOT).contains("luca")) {
                    budget = value * 1000;
                } else {
                    diners = value;
                }
            }
        }

        boolean knowsDiners = diners > 0 || !known.contains("sin definir");
        boolean knowsBudget = budget > 0 || known.contains("por persona");

        String action;
        String say;
        if (hasCombo) {
            action = "AJUSTAR";
            say = "Listo, lo ajusto y lo vuelvo a cotizar.";
        } else if (knowsDiners && knowsBudget) {
            action = "CONSTRUIR";
            say = "Perfecto, les armo el combo.";
        } else if (!knowsDiners) {
            action = "PREGUNTAR";
            say = "Cuantos comensales son?";
        } else {
            action = "PREGUNTAR";
            say = "Cuanto quieren gastar por persona?";
        }

        String low = said.toLowerCase(Locale.ROOT);
        String kitchen = "-";
        for (String tradition : Kitchens.traditions()) {
            if (low.contains(tradition)) {
                kitchen = tradition;
            }
        }
        if (action.equals("CONSTRUIR") && kitchen.equals("-")) {
            kitchen = low.contains("liviano") ? "japonesa" : "colombiana";
        }

        String change = "-";
        for (String part : new String[]{"entrada", "postre", "bebida", "fuerte"}) {
            if (low.contains(part)) {
                change = part;
            }
        }

        return "ACCION: " + action
             + "\nDECIR: " + say
             + "\nCOMENSALES: " + (diners > 0 ? diners : "-")
             + "\nPRESUPUESTO: " + (budget > 0 ? budget : "-")
             + "\nOCASION: -"
             + "\nRESTRICCIONES: " + (low.contains("no com") ? said.trim() : "-")
             + "\nCOCINA: " + kitchen
             + "\nCAMBIAR: " + change;
    }

    // ------------------------------------------------------------------ rol 2

    private String asChef(String prompt) {
        String tradition = "colombiana";
        for (String candidate : Kitchens.traditions()) {
            if (prompt.contains("cocina " + candidate)) {
                tradition = candidate;
            }
        }
        String role = "Entrada";
        for (String candidate : new String[]{"Plato fuerte", "Postre", "Bebida", "Entrada"}) {
            if (prompt.contains("combo: " + candidate)) {
                role = candidate;
            }
        }
        String[] dish = canned(tradition, role);
        return "NOMBRE: " + dish[0] + "\nINGREDIENTES: " + dish[1] + "\nPASOS: " + dish[2];
    }

    private static String[] canned(String tradition, String role) {
        return switch (tradition + "/" + role) {
            case "japonesa/Entrada" -> new String[]{"Sunomono de pepino",
                    "pepino | vinagre de arroz | alga wakame | ajonjoli",
                    "cortar el pepino fino | salar y escurrir | mezclar con el vinagre | enfriar"};
            case "japonesa/Plato fuerte" -> new String[]{"Donburi de tofu",
                    "arroz japones | tofu | salsa de soya | cebollin",
                    "cocer el arroz | dorar el tofu | glasear con soya | montar en cuenco"};
            case "japonesa/Postre" -> new String[]{"Mochi de matcha",
                    "arroz glutinoso | matcha | frijol rojo dulce",
                    "cocer al vapor | amasar en frio | porcionar | rellenar"};
            case "japonesa/Bebida" -> new String[]{"Te de cebada tostada",
                    "cebada tostada | jengibre",
                    "tostar la cebada | infusionar diez minutos | colar | servir frio"};
            case "italiana/Entrada" -> new String[]{"Bruschetta de tomate",
                    "pan rustico | tomate | albahaca | aceite de oliva",
                    "tostar el pan | picar el tomate | frotar ajo | montar y aceitar"};
            case "italiana/Plato fuerte" -> new String[]{"Cacio e pepe",
                    "espagueti | pecorino | aceite de oliva",
                    "cocer la pasta | tostar la pimienta | emulsionar el queso | ligar fuera del fuego"};
            case "italiana/Postre" -> new String[]{"Crema al cacao",
                    "crema | cacao | limon",
                    "calentar la crema | disolver el cacao | moldear | enfriar"};
            case "italiana/Bebida" -> new String[]{"Tinto de la casa",
                    "vino tinto",
                    "abrir treinta minutos antes | servir a dieciseis grados"};
            case "colombiana/Entrada" -> new String[]{"Arepa de choclo con quesito",
                    "maiz tierno | quesito | cebolla larga",
                    "moler el maiz | armar las arepas | asar en budare | abrir y rellenar"};
            case "colombiana/Plato fuerte" -> new String[]{"Sudado de pollo",
                    "pollo | papa | guiso | cilantro",
                    "hacer el guiso | sellar el pollo | agregar papa y agua | cocinar a fuego bajo"};
            case "colombiana/Postre" -> new String[]{"Brevas con arequipe",
                    "breva | panela | arequipe",
                    "cocer las brevas en agua de panela | dejar reposar | rellenar con arequipe"};
            case "colombiana/Bebida" -> new String[]{"Limonada de panela",
                    "limon | panela",
                    "derretir la panela | exprimir el limon | mezclar | servir en jarra"};
            default -> new String[]{"plato de la casa", "papa | cebolla larga", "cocinar | servir"};
        };
    }

    // ------------------------------------------------------------------ texto

    private static String line(String text, String prefix) {
        for (String raw : text.split("\n")) {
            if (raw.trim().startsWith(prefix)) {
                return raw.trim().substring(prefix.length()).trim();
            }
        }
        return "";
    }

    /** El ultimo "cliente: ..." del prompt es lo que acaba de decir. */
    private static String after(String text, String marker) {
        int at = text.lastIndexOf(marker);
        if (at < 0) {
            return "";
        }
        String rest = text.substring(at + marker.length());
        int end = rest.indexOf('\n');
        return end < 0 ? rest.trim() : rest.substring(0, end).trim();
    }
}
