package com.dumensel.payment.domain.model;

import java.util.regex.Pattern;

/**
 * Card Information Value Object
 * CRITICAL: Bu sınıf hassas veriler içerir. LOG'LANMAMALI!
 */
public class CardInfo {
    private final String cardHolderName;
    private final String cardNumber; // Encrypted in persistence
    private final String expireMonth;
    private final String expireYear;
    private final String cvv; // NEVER persist or log!

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");

    public CardInfo(String cardHolderName, String cardNumber, String expireMonth, 
                    String expireYear, String cvv) {
        // Validation
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Card holder name is required");
        }
        if (!isValidCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number format");
        }
        if (!isValidExpireDate(expireMonth, expireYear)) {
            throw new IllegalArgumentException("Invalid expire date");
        }
        if (!isValidCVV(cvv)) {
            throw new IllegalArgumentException("Invalid CVV");
        }

        this.cardHolderName = cardHolderName.trim().toUpperCase();
        this.cardNumber = cardNumber.replaceAll("\\s+", ""); // Remove spaces
        this.expireMonth = expireMonth;
        this.expireYear = expireYear;
        this.cvv = cvv;
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) return false;
        String normalized = cardNumber.replaceAll("\\s+", "");
        return CARD_NUMBER_PATTERN.matcher(normalized).matches() && passesLuhnCheck(normalized);
    }

    // Luhn algorithm for card validation
    private boolean passesLuhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private boolean isValidExpireDate(String month, String year) {
        if (month == null || year == null) return false;
        try {
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);
            return m >= 1 && m <= 12 && year.length() == 4 && y >= 2024;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidCVV(String cvv) {
        return cvv != null && CVV_PATTERN.matcher(cvv).matches();
    }

    // Getters
    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpireMonth() {
        return expireMonth;
    }

    public String getExpireYear() {
        return expireYear;
    }

    public String getCvv() {
        return cvv;
    }

    // Maskelenmiş kart numarası (güvenli gösterim için)
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "************" + lastFour;
    }

    // GÜVENLİK: toString'de hassas bilgi gösterme!
    @Override
    public String toString() {
        return "CardInfo{maskedNumber=" + getMaskedCardNumber() + "}";
    }
}

