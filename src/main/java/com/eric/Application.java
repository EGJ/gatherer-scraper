package com.eric;

import com.eric.common.Props;
import com.eric.common.Utility;
import com.eric.model.CardInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Application {

    public static void main(String[] args) throws IOException {
        searchForCards();

        if (Boolean.parseBoolean(Props.properties.getProperty("print-valid-commanders"))) {
            printValidCommanders();
        }
    }

    private static void searchForCards() throws IOException {
        Set<CardInfo> cachedValues = new HashSet<>(readCachedValues());

        Set<String> cardsToSearchFor = new HashSet<>(FileUtils.readLines(new File("src/main/resources/CardsToSearchFor.txt"), "UTF-8"));
        Set<String> invalidCardNames = new HashSet<>();

        Set<CardInfo> cardInfoSet = cardsToSearchFor.stream()
                .filter(cardName -> !cachedValues.contains(new CardInfo(cardName, null, null, null, null, null, null, null)) && !cardName.isBlank())
                .map(cardName -> {
                    //For all split cards, get the name of the first portion
                    String cardNameToSearchFor = cardName.replaceFirst(" ?//.*", "");
                    CardInfo cardInfo = WebScraper.getCardDetails(cardNameToSearchFor);

                    if (cardInfo == null) {
                        invalidCardNames.add(cardName);
                    } else {
                        cardInfo.setCardName(cardName);
                    }
                    return cardInfo;
                })
                .collect(Collectors.toSet());
        cardInfoSet.remove(null);

        WebScraper.quitDriver();


        if (invalidCardNames.size() != 0) {
            System.out.println("Invalid Card names:");
            invalidCardNames.forEach(System.out::println);
        }

        FileWriter masterWriter = new FileWriter("output/Master.txt", true);
        for (CardInfo cardAndCost : cardInfoSet) {
            //TODO?: Allow sorting by other methods.
            FileWriter writer = new FileWriter("output/cards/" + cardAndCost.getColorIdentity() + ".txt", true);
            masterWriter.append(cardAndCost.getCardName()).append('\n');
            writer.append(cardAndCost.getCardName()).append('\n');
            writer.close();
        }
        masterWriter.close();

        cachedValues.addAll(cardInfoSet);
        writeCachedValues(cachedValues);
    }

    private static void printValidCommanders() {
        String validColors = Props.properties.getProperty("print-valid-commanders.color-identity");
        boolean strictColorIdentity = Boolean.parseBoolean(Props.properties.getProperty("print-valid-commanders.strict-color-identity"));

        StringBuilder colorIdentity = new StringBuilder();

        if (validColors.isBlank()) {
            Utility.colors.forEach(color -> {
                colorIdentity.append(color);
                colorIdentity.append(",");
            });
        } else {
            Utility.colors.forEach(color -> Parser.addToIdentityIfContained(validColors, colorIdentity, color));
        }

        if (!strictColorIdentity || validColors.equalsIgnoreCase("Colorless")) {
            colorIdentity.append("Colorless");
        } else if (colorIdentity.length() == 0) {
            System.out.println("No recognized colors in color identity.");
        } else {
            //Remove trailing ","
            colorIdentity.delete(colorIdentity.length() - 1, colorIdentity.length());
        }

        readCachedValues().stream()
                .filter(CardInfo::getValidCommander)
                .filter(cardInfo -> {
                    if (strictColorIdentity) {
                        return cardInfo.getColorIdentity().equals(colorIdentity.toString());
                    } else {
                        return colorIdentity.toString().contains(cardInfo.getColorIdentity());
                    }
                })
                .forEach(cardInfo -> System.out.println(cardInfo.getCardName()));
    }

    private static void writeCachedValues(Set<CardInfo> augmentedCardNames) throws IOException {
        File fileToWriteTo = new File("output/CachedValues.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(fileToWriteTo, augmentedCardNames);
    }

    private static Set<CardInfo> readCachedValues() {
        File fileToReadFrom = new File("output/CachedValues.json");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(FileUtils.readFileToByteArray(fileToReadFrom), new TypeReference<Set<CardInfo>>() {
            });
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    //Used for testing.
    //Cached Value:
    //[Abbey Gargoyles, "Rumors of My Death . . .", _____, Abandon Reason, Aboroth, Agent of Masks, Absorb, Adriana, Captain of the Guard, Advent of the Wurm, Agony Warp, Acidic Sliver, Abrupt Decay, Adeliz, the Cinder Wind, Aeromunculus, "Ach! Hans, Run!", Aminatou, the Fateshifter, Ankle Shanker, Abzan Ascendancy, Efreet Weaponmaster, Angus Mackenzie, Fiery Justice, Admiral Beckett Brass, Abomination of Gudul, Adun Oakenshield, Animar, Soul of Elements, Breya, Etherium Shaper, Atraxa, Praetors' Voice, Dune-Brood Nephilim, Ink-Treader Nephilim, Glint-Eye Nephilim, Atogatog]
    private static void getCardOfEveryColorCombination() {
        //E.G. http://gatherer.wizards.com/Pages/Search/Default.aspx?color=+@([W]+[U]+[B])
        Stream<String> colorCombinations = "W\nB\nU\nR\nG\nW,B\nW,U\nW,R\nW,G\nB,U\nB,R\nB,G\nU,R\nU,G\nR,G\nW,B,U\nW,B,R\nW,B,G\nW,U,R\nW,U,G\nW,R,G\nB,U,R\nB,U,G\nB,R,G\nU,R,G\nW,B,U,R\nW,B,U,G\nW,B,R,G\nW,U,R,G\nB,U,R,G\nW,B,U,R,G".lines();

        List<String> searchStrings = colorCombinations.map(colorCombination -> {
            String gathererString = "([" + colorCombination + "])";
            return gathererString.replaceAll(",", "]+[");
        }).collect(Collectors.toList());

        List<String> cardTitles = WebScraper.getCardTitles(searchStrings);
        System.out.println("cardTitles = " + cardTitles);
        cardTitles.forEach(System.out::println);
        WebScraper.quitDriver();
    }
}
