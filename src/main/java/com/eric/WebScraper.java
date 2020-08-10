package com.eric;


import com.eric.common.Props;
import com.eric.model.CardInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class WebScraper {

    private static WebDriver driver;
    private static final int driverSleepMillis = Integer.parseInt(Props.properties.getProperty("webdriver.sleep.millis"));

    static {
        System.setProperty("webdriver.gecko.driver", Props.properties.getProperty("webdriver.gecko.driver.location"));

        driver = new FirefoxDriver();
        //Only waits for initial page load
        //driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    static CardInfo getCardDetails(String cardName) {
        try {
            TimeUnit.MILLISECONDS.sleep(driverSleepMillis);
        } catch (InterruptedException e) {
            //
        }
        driver.get("http://gatherer.wizards.com/Pages/Card/Details.aspx?name=" + cardName);


        String cardType;
        String manaCost;
        String colorIndicator;
        String rulesTextColorIdentity;
        String manaCostColorIdentity;
        String colorIdentity = null;
        boolean validCommander;

        try {
            cardType = getCardType();

            if (cardType.equals("Land")) {
                manaCost = "0";
                colorIdentity = "Land";
            } else {
                manaCost = Parser.parseCosts(getManaRowStrings());
            }
        } catch (NoSuchElementException e) {
            return null;
        }
        List<String> rulesTextStrings = getRulesTextStrings();

        colorIndicator = getColorIndicator();
        manaCostColorIdentity = Parser.getColorIdentity(manaCost);
        rulesTextColorIdentity = getRulesTextColorIdentity(rulesTextStrings);
        if (colorIdentity == null) {
            colorIdentity = Parser.getColorIdentity(manaCostColorIdentity + rulesTextColorIdentity + colorIndicator);
        }

        //When determining whether or not a commander is valid, only consider the first part of the card if it is a flip card
        String commanderCardType = cardType.split("//")[0];
        validCommander = commanderCardType.contains("Legendary") && commanderCardType.contains("Creature");
        validCommander |= isValidPlaneswalkerCommander(rulesTextStrings);

        return new CardInfo(cardName, cardType, manaCost, colorIdentity, colorIndicator, manaCostColorIdentity, rulesTextColorIdentity, validCommander);
    }

    //Will throw an exception if the card name is invalid
    private static String getCardType() {
        return driver.findElements(By.cssSelector("[id$=typeRow]")).stream()
                .map(webElement -> webElement.findElement(By.className("value")).getAttribute("innerHTML").stripLeading())
                .collect(Collectors.joining(" // "));
    }

    private static List<String> getManaRowStrings() {
        return driver.findElements(By.cssSelector("[id$=manaRow]")).stream()
                .map(webElement -> webElement.findElement(By.className("value")).getAttribute("innerHTML"))
                .map(manaRow -> manaRow.replaceAll("Variable Colorless", "X"))
                .collect(Collectors.toList());
    }

    private static String getColorIndicator() {
        try {
            return driver.findElement(By.cssSelector("[id$=colorIndicatorRow]")).findElement(By.className("value")).getAttribute("innerHTML").stripLeading();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private static List<String> getRulesTextStrings() {
        return driver.findElements(By.cssSelector("[id$=textRow]")).stream()
                .map(webElement -> webElement.findElement(By.className("value")).getAttribute("innerHTML"))
                .collect(Collectors.toList());
    }

    private static String getRulesTextColorIdentity(List<String> rulesTextStrings) {
        String rulesTextCosts = Parser.parseCosts(rulesTextStrings);

        if (rulesTextCosts == null) {
            return null;
        } else {
            return Parser.getColorIdentity(rulesTextCosts);
        }
    }

    private static boolean isValidPlaneswalkerCommander(List<String> rulesTextStrings) {
        return rulesTextStrings.stream().anyMatch(rulesTextString -> rulesTextString.contains("can be your commander"));
    }

    //Returns a list of card names. One for each color combination given.
    static ArrayList<String> getCardTitles(List<String> colorCombinations) {
        ArrayList<String> elements = new ArrayList<>(colorCombinations.size());
        for (String colorCombination : colorCombinations) {
            driver.get("http://gatherer.wizards.com/Pages/Search/Default.aspx?color=+@" + colorCombination);

            String cardName = driver.findElement(By.className("cardTitle")).findElement(By.tagName("a")).getAttribute("innerHTML");
            elements.add(cardName);

            try {
                TimeUnit.MILLISECONDS.sleep(driverSleepMillis);
            } catch (InterruptedException e) {
                //
            }
        }

        return elements;
    }

    static void quitDriver() {
        driver.quit();
    }
}