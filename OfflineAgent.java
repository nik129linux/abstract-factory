import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The same system with no model.
 *
 * Not part of the assignment: it is the fallback for the presentation.
 * gemma4:31b-cloud goes through ollama but the model runs off the machine, so
 * with no internet in the classroom the interface would die halfway through the
 * demo.
 *
 * It answers in the same formats the model does. It orients itself by the first
 * line of the prompt, which states the role -- that is why every prompt opens
 * with "ROLE:". Neither the waiter nor the chef notices the swap.
 */
public class OfflineAgent implements Agent {

    private static final Pattern NUMBER = Pattern.compile("\\d[\\d.]*");

    /** The model reads "six of us"; a regex does not, so it gets a small table. */
    private static final String[] WORDS = {
        "one", "two", "three", "four", "five", "six",
        "seven", "eight", "nine", "ten", "eleven", "twelve"};

    private String lastPrompt = "";
    private String lastReply = "";

    @Override
    public String name() {
        return "offline (no model)";
    }

    @Override
    public String ask(String prompt) {
        lastPrompt = prompt;
        lastReply = prompt.startsWith("ROLE: waiter") ? asWaiter(prompt) : asChef(prompt);
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

    // ------------------------------------------------------------------ role 1

    private String asWaiter(String prompt) {
        String said = after(prompt, "client: ");
        String known = line(prompt, "Order so far:");
        boolean hasCombo = !line(prompt, "Current combo:").contains("none");

        int diners = 0;
        int budget = 0;
        String spelled = said.toLowerCase(Locale.ROOT);
        for (int i = 0; i < WORDS.length; i++) {
            if (spelled.matches(".*\\b" + WORDS[i] + "\\b.*")) {
                diners = i + 1;
            }
        }
        Matcher m = NUMBER.matcher(said);
        while (m.find()) {
            int value = Integer.parseInt(m.group().replace(".", ""));
            if (value >= 1000) {
                budget = value;
            } else if (value >= 1 && value <= 100) {
                if (said.toLowerCase(Locale.ROOT).contains("grand")) {
                    budget = value * 1000;
                } else {
                    diners = value;
                }
            }
        }

        boolean knowsDiners = diners > 0 || !known.contains("not set");
        boolean knowsBudget = budget > 0 || known.contains("per person");

        String action;
        String say;
        if (hasCombo) {
            action = "ADJUST";
            say = "Right, I will rework it and quote it again.";
        } else if (knowsDiners && knowsBudget) {
            action = "BUILD";
            say = "Perfect, I will put the combo together.";
        } else if (!knowsDiners) {
            action = "ASK";
            say = "How many diners are you?";
        } else {
            action = "ASK";
            say = "How much do you want to spend per person?";
        }

        String low = said.toLowerCase(Locale.ROOT);
        String kitchen = "-";
        for (String tradition : Kitchens.traditions()) {
            if (low.contains(tradition)) {
                kitchen = tradition;
            }
        }
        if (action.equals("BUILD") && kitchen.equals("-")) {
            kitchen = low.contains("light") ? "japanese" : "colombian";
        }

        String change = "-";
        for (String part : new String[]{"starter", "dessert", "drink", "main"}) {
            if (low.contains(part)) {
                change = part;
            }
        }

        return "ACTION: " + action
             + "\nSAY: " + say
             + "\nDINERS: " + (diners > 0 ? diners : "-")
             + "\nBUDGET: " + (budget > 0 ? budget : "-")
             + "\nOCCASION: -"
             + "\nRESTRICTIONS: " + (low.contains("no ") ? said.trim() : "-")
             + "\nKITCHEN: " + kitchen
             + "\nCHANGE: " + change;
    }

    // ------------------------------------------------------------------ role 2

    private String asChef(String prompt) {
        String tradition = "colombian";
        for (String candidate : Kitchens.traditions()) {
            if (prompt.contains("a " + candidate + " catering")) {
                tradition = candidate;
            }
        }
        String role = "Starter";
        for (String candidate : new String[]{"Main", "Dessert", "Drink", "Starter"}) {
            if (prompt.contains("combo is: " + candidate)) {
                role = candidate;
            }
        }
        String[] dish = canned(tradition, role);
        return "NAME: " + dish[0] + "\nINGREDIENTS: " + dish[1] + "\nSTEPS: " + dish[2];
    }

    private static String[] canned(String tradition, String role) {
        return switch (tradition + "/" + role) {
            case "japanese/Starter" -> new String[]{"Cucumber sunomono",
                    "cucumber | rice vinegar | wakame seaweed | sesame",
                    "slice the cucumber thin | salt and drain | toss with the vinegar | chill"};
            case "japanese/Main" -> new String[]{"Tofu donburi",
                    "japanese rice | tofu | soy sauce | spring onion",
                    "cook the rice | brown the tofu | glaze with soy | serve in a bowl"};
            case "japanese/Dessert" -> new String[]{"Matcha mochi",
                    "glutinous rice | matcha | sweet red bean",
                    "steam it | knead it cold | portion it | fill it"};
            case "japanese/Drink" -> new String[]{"Roasted barley tea",
                    "roasted barley | ginger",
                    "toast the barley | steep ten minutes | strain | serve cold"};
            case "italian/Starter" -> new String[]{"Tomato bruschetta",
                    "rustic bread | tomato | basil | olive oil",
                    "toast the bread | dice the tomato | rub with garlic | assemble and oil"};
            case "italian/Main" -> new String[]{"Cacio e pepe",
                    "spaghetti | pecorino | olive oil",
                    "cook the pasta | toast the pepper | emulsify the cheese | bind off the heat"};
            case "italian/Dessert" -> new String[]{"Cocoa cream",
                    "cream | cocoa | lemon",
                    "heat the cream | dissolve the cocoa | mould it | chill"};
            case "italian/Drink" -> new String[]{"House red",
                    "red wine",
                    "open it thirty minutes ahead | serve at sixteen degrees"};
            case "colombian/Starter" -> new String[]{"Sweet corn arepa with fresh cheese",
                    "sweet corn | fresh cheese | long onion",
                    "grind the corn | shape the arepas | griddle them | split and fill"};
            case "colombian/Main" -> new String[]{"Chicken sudado",
                    "chicken | potato | sofrito | coriander",
                    "make the sofrito | sear the chicken | add potato and water | simmer low"};
            case "colombian/Dessert" -> new String[]{"Figs with arequipe",
                    "fig | panela | arequipe",
                    "poach the figs in panela water | let them rest | fill with arequipe"};
            case "colombian/Drink" -> new String[]{"Panela lemonade",
                    "lemon | panela",
                    "melt the panela | squeeze the lemons | mix | serve in a jug"};
            default -> new String[]{"house dish", "potato | long onion", "cook | serve"};
        };
    }

    // ------------------------------------------------------------------- text

    private static String line(String text, String prefix) {
        for (String raw : text.split("\n")) {
            if (raw.trim().startsWith(prefix)) {
                return raw.trim().substring(prefix.length()).trim();
            }
        }
        return "";
    }

    /** The last "client: ..." in the prompt is what was just said. */
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
