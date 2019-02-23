package com.eric;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Parser {

    private static Pattern pattern = Pattern.compile("alt=(.*?) align");

    private static ArrayList<String> colors = new ArrayList<>(5) {{
        add("Black");
        add("Blue");
        add("Green");
        add("Red");
        add("White");
    }};

    private static ArrayList<String> parseCostsIndividually(List<String> strings) {
        ArrayList<String> manaCosts = new ArrayList<>();

        for (String string : strings) {
            Matcher matcher = pattern.matcher(string);

            StringBuilder manaCost = new StringBuilder();
            while (matcher.find()) {
                String group = matcher.group(1).replaceAll("\"", "");
                //Ignore all Non-mana symbols (e.g. Tap/Untap symbols)
                if (!group.matches("\\d+") && !group.equals("X")) {
                    long colorsContained = colors.stream().filter(group::contains).count();
                    if(colorsContained == 0){
                        continue;
                    }
                }

                manaCost.append(group);
                manaCost.append(", ");
            }
            if (manaCost.length() != 0) {
                //Remove trailing ", "
                manaCost.delete(manaCost.length() - 2, manaCost.length());

                manaCosts.add(manaCost.toString());
            }
        }

        return manaCosts;
    }

    static String parseCosts(List<String> strings) {
        List<String> manaCosts = parseCostsIndividually(strings);

        StringBuilder manaCostString = new StringBuilder();
        for (String manaCost : manaCosts) {
            manaCostString.append(manaCost);
            manaCostString.append(" // ");
        }

        if (manaCostString.length() != 0) {
            //Remove trailing " // "
            manaCostString.delete(manaCostString.length() - 4, manaCostString.length());
            return manaCostString.toString();
        } else {
            return null;
        }
    }

    static String getColorIdentity(String manaCost) {
        if (manaCost == null) {
            return null;
        } else {
            StringBuilder colorIdentity = new StringBuilder();
            colors.forEach(color -> addToIdentityIfContained(manaCost, colorIdentity, color));
            if (colorIdentity.length() != 0) {
                //Remove trailing ","
                colorIdentity.delete(colorIdentity.length() - 1, colorIdentity.length());
                return colorIdentity.toString();
            } else {
                return "Colorless";
            }
        }
    }

    private static void addToIdentityIfContained(String manaCost, StringBuilder colorIdentity, String color) {
        if (manaCost.contains(color)) {
            colorIdentity.append(color);
            colorIdentity.append(',');
        }
    }
}
