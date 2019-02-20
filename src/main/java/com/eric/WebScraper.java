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

class WebScraper {

    private static WebDriver driver;
    private static final int driverSleepMillis = Integer.parseInt(Props.properties.getProperty("webdriver.sleep.millis"));

    static {
        System.setProperty("webdriver.gecko.driver", "C:\\Program Files (x86)\\Selenium-Webdriver\\geckodriver-v0.23.0-win64\\geckodriver.exe");

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

        try {
            cardType = getCardType();

            if (cardType.contains("Land")) {
                manaCost = "0";
                colorIdentity = "Land";
            } else {
                manaCost = Parser.parseCosts(getManaRowStrings());
            }
        } catch (NoSuchElementException e) {
            return null;
        }

        colorIndicator = getColorIndicator();
        manaCostColorIdentity = Parser.getColorIdentity(manaCost);
        rulesTextColorIdentity = getRulesTextColorIdentity();
        if (colorIdentity == null) {
            colorIdentity = Parser.getColorIdentity(manaCostColorIdentity + rulesTextColorIdentity + colorIndicator);
        }

        return new CardInfo(cardName, cardType, manaCost, colorIdentity, colorIndicator, manaCostColorIdentity, rulesTextColorIdentity);
    }

    //Will throw an exception if the card name is invalid
    private static String getCardType() {
        try {
            return driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_typeRow")).findElement(By.className("value")).getAttribute("innerHTML").stripLeading();
        } catch (NoSuchElementException e) {
            String cardType1 = driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl02_typeRow")).findElement(By.className("value")).getAttribute("innerHTML").stripLeading();
            String cardType2 = driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl03_typeRow")).findElement(By.className("value")).getAttribute("innerHTML").stripLeading();

            return cardType1 + " // " + cardType2;
        }
    }

    private static ArrayList<String> getManaRowStrings() {
        ArrayList<String> manaRowValues = new ArrayList<>(2);
        //Try to get the mana cost of a "Normal" Card
        try {
            String manaRowValue = driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_manaRow")).findElement(By.className("value")).getAttribute("innerHTML");

            manaRowValues.add(manaRowValue.replaceAll("Variable Colorless", "X"));
        } catch (NoSuchElementException e) {
            //If That failed, the mana cost of any card with a mana cost will be found in the first alternate manaRow (E.g. // cards and flip cards)
            //Lands will cause this to throw a NoSuchElementException
            String manaRowValue1 = driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl02_manaRow")).findElement(By.className("value")).getAttribute("innerHTML");
            manaRowValues.add(manaRowValue1.replaceAll("Variable Colorless", "X"));

            //Try to get the second alternate manaRow (E.g. the second half of a // card)
            try {
                String manaRowValue2 = driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl03_manaRow")).findElement(By.className("value")).getAttribute("innerHTML");
                manaRowValues.add(manaRowValue2.replaceAll("Variable Colorless", "X"));
            } catch (NoSuchElementException e2) {
                //Card does not have a second alternate manaRow, ignore.
            }
        }

        return manaRowValues;
    }

    //Most cards with a color indicator will be flip cards, e.g. Nicol Bolas, the Arisen. So the ct103 colorIndicator will be searched first.
    //Other cards with a color indicator, e.g. Evermind, will have them on the front - though this seems to be incredibly rare.
    private static String getColorIndicator() {
        try {
            return driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl03_colorIndicatorRow")).findElement(By.className("value")).getAttribute("innerHTML").stripLeading();
        } catch (NoSuchElementException e) {
            try {
                return driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_colorIndicatorRow")).findElement(By.className("value")).getAttribute("innerHTML").stripLeading();
            } catch (NoSuchElementException e2) {
                return null;
            }
        }
    }

    private static String getRulesTextColorIdentity() {
        ArrayList<String> rulesTextStrings = new ArrayList<>(2);
        //Try to get the rules text cost of a "Normal" Card
        try {
            String rulesTextString = driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_textRow")).findElement(By.className("value")).getAttribute("innerHTML");

            rulesTextStrings.add(rulesTextString);
        } catch (NoSuchElementException e) {
            try {
                String rulesTextString1 = driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl02_textRow")).findElement(By.className("value")).getAttribute("innerHTML");
                String rulesTextString2 = driver.findElement(By.id("ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl03_textRow")).findElement(By.className("value")).getAttribute("innerHTML");

                rulesTextStrings.add(rulesTextString1);
                rulesTextStrings.add(rulesTextString2);
            } catch (NoSuchElementException e2) {
                //Card has no rules text
                return null;
            }
        }

        String rulesTextCosts = Parser.parseCosts(rulesTextStrings);

        if (rulesTextCosts == null) {
            return null;
        } else {
            return Parser.getColorIdentity(rulesTextCosts);
        }
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