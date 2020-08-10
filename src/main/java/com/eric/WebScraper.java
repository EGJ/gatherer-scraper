package com.eric;


import com.eric.common.Props;
import com.eric.model.CardInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    static CardInfo getCardDetails(String shortCardName) {
        try {
            TimeUnit.MILLISECONDS.sleep(driverSleepMillis);
        } catch (InterruptedException e) {
            //
        }
        driver.get("http://gatherer.wizards.com/Pages/Card/Details.aspx?name=" + shortCardName);

        String fullCardName;
        String cardType;
        String manaCost;
        String colorIndicator;
        String rulesTextColorIdentity;
        String manaCostColorIdentity;
        String colorIdentity = null;
        boolean validCommander;

        try {
            fullCardName = getCardName();
            saveCardImages(shortCardName);
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

        return new CardInfo(fullCardName, cardType, manaCost, colorIdentity, colorIndicator, manaCostColorIdentity, rulesTextColorIdentity, validCommander);
    }

    /**
     * This can also be done via right-click -> save image (i.e. new Actions(driver).contextClick(webElement)...).
     * As far as I can tell, this would avoid additional web requests, but will probably interfere with other tasks
     * if the user is running this program in the background, for example.
     *
     * Note: Split cards such as "Grind // Dust" and all other cards with "//" in their name will be saved using the
     * first portion of the cards name due to filename restrictions.
     */
    private static void saveCardImages(String cardName) {
        File file = new File("output/cards/images/" + cardName + ".png");

        //Only extract and save images if it has not already been done and the appropriate property is enabled.
        if (file.exists() || !Boolean.parseBoolean(Props.properties.getProperty("extract-images"))) {
            return;
        }

        driver.findElements(By.cssSelector("[id$=cardImage]")).stream()
                .map(webElement -> webElement.getAttribute("src"))
                .distinct()//Prevents cards such as "Wear // Tear" and other split cards from being pulled twice
                .map(url -> {
                    try {
                        URL imageURL = new URL(url);
                        return ImageIO.read(imageURL);
                    } catch (IOException ex) {
                        throw new RuntimeException("Unable to read image: ", ex);
                    }
                })
                .filter(Objects::nonNull)
                .reduce(WebScraper::joinBufferedImage)
                .ifPresent(image -> {
                    try {
                        ImageIO.write(image, "png", file);
                    } catch (IOException ex) {
                        throw new RuntimeException("Unable to save image: ", ex);
                    }
                });
    }

    public static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth() + img2.getWidth();
        int height = Math.max(img1.getHeight(), img2.getHeight());
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = newImage.createGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, img1.getWidth(), 0, null);
        g.dispose();
        return newImage;
    }

    private static String getCardName() {
        return driver.findElement(By.cssSelector("[id=ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_subtitleDisplay]")).getAttribute("innerHTML");
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