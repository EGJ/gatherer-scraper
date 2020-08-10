package com.eric;

import com.eric.common.Utility;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Parser {

    private static Pattern pattern = Pattern.compile("alt=(.*?) align");

    static String parseCosts(List<String> strings) {
        if (strings.size() == 0) {
            return "0";
        }

        return strings.stream()
                .map(string -> {
                    Matcher matcher = pattern.matcher(string);

                    StringBuilder manaCost = new StringBuilder();
                    while (matcher.find()) {
                        String group = matcher.group(1).replaceAll("\"", "");
                        //Ignore all Non-mana symbols (e.g. Tap/Untap symbols)
                        if (!group.matches("\\d+") && !group.equals("X")) {
                            long colorsContained = Utility.colors.stream().filter(group::contains).count();
                            if (colorsContained == 0) {
                                continue;
                            }
                        }

                        manaCost.append(group);
                        manaCost.append(", ");
                    }
                    if (manaCost.length() != 0) {
                        //Remove trailing ", "
                        manaCost.delete(manaCost.length() - 2, manaCost.length());
                    }

                    return manaCost.toString();
                })
                .collect(Collectors.joining(" // "));
    }

    static String getColorIdentity(String manaCost) {
        if (manaCost == null) {
            return null;
        } else {
            StringBuilder colorIdentity = new StringBuilder();
            Utility.colors.forEach(color -> addToIdentityIfContained(manaCost, colorIdentity, color));
            if (colorIdentity.length() != 0) {
                //Remove trailing ","
                colorIdentity.delete(colorIdentity.length() - 1, colorIdentity.length());
                return colorIdentity.toString();
            } else {
                return "Colorless";
            }
        }
    }

    static void addToIdentityIfContained(String manaCost, StringBuilder colorIdentity, String color) {
        if (manaCost.toLowerCase().contains(color.toLowerCase())) {
            colorIdentity.append(color);
            colorIdentity.append(',');
        }
    }
}
