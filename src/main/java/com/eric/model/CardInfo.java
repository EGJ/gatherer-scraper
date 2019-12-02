package com.eric.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class CardInfo {
    private String cardName;
    private String cardType;
    private String manaCost;
    private String colorIdentity;
    private String colorIndicator;
    private String manaCostColorIdentity;
    private String rulesTextColorIdentity;
    private Boolean validCommander;

    @JsonCreator
    public CardInfo(@JsonProperty("cardName") String cardName,
                    @JsonProperty("cardType") String cardType,
                    @JsonProperty("cost") String manaCost,
                    @JsonProperty("colorIdentity") String colorIdentity,
                    @JsonProperty("colorIndicator") String colorIndicator,
                    @JsonProperty("manaCostColorIdentity") String manaCostColorIdentity,
                    @JsonProperty("rulesTextColorIdentity") String rulesTextColorIdentity,
                    @JsonProperty("validCommander") Boolean validCommander) {
        this.cardName = cardName;
        this.cardType = cardType;
        this.manaCost = manaCost;
        this.colorIdentity = colorIdentity;
        this.colorIndicator = colorIndicator;
        this.manaCostColorIdentity = manaCostColorIdentity;
        this.rulesTextColorIdentity = rulesTextColorIdentity;
        this.validCommander = validCommander;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardType() {
        return cardType;
    }

    public String getManaCost() {
        return manaCost;
    }

    public String getColorIdentity() {
        return colorIdentity;
    }

    public String getColorIndicator() {
        return colorIndicator;
    }

    public String getManaCostColorIdentity() {
        return manaCostColorIdentity;
    }

    public String getRulesTextColorIdentity() {
        return rulesTextColorIdentity;
    }

    public Boolean getValidCommander() {
        return validCommander;
    }

    @Override
    public String toString() {
        return cardName + " (" + manaCost + ") [" + colorIdentity + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardInfo other = (CardInfo) o;
        return cardName.toLowerCase().equals(other.cardName.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardName.toLowerCase());
    }
}
