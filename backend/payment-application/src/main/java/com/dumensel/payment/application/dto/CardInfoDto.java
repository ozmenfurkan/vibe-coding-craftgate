package com.dumensel.payment.application.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for card information
 * CRITICAL: Bu sınıf hassas veriler içerir. LOG'LANMAMALI!
 */
public class CardInfoDto {
    
    @NotBlank(message = "Card holder name is required")
    @Size(min = 2, max = 100, message = "Card holder name must be between 2 and 100 characters")
    private String cardHolderName;
    
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number format")
    private String cardNumber;
    
    @NotBlank(message = "Expire month is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Invalid expire month")
    private String expireMonth;
    
    @NotBlank(message = "Expire year is required")
    @Pattern(regexp = "^20[2-9][0-9]$", message = "Invalid expire year")
    private String expireYear;
    
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV format")
    private String cvv;
    
    // Constructors
    public CardInfoDto() {
    }

    public CardInfoDto(String cardHolderName, String cardNumber, 
                      String expireMonth, String expireYear, String cvv) {
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
        this.expireMonth = expireMonth;
        this.expireYear = expireYear;
        this.cvv = cvv;
    }

    // Getters and Setters
    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpireMonth() {
        return expireMonth;
    }

    public void setExpireMonth(String expireMonth) {
        this.expireMonth = expireMonth;
    }

    public String getExpireYear() {
        return expireYear;
    }

    public void setExpireYear(String expireYear) {
        this.expireYear = expireYear;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    // GÜVENLİK: toString'de hassas bilgi gösterme!
    @Override
    public String toString() {
        return "CardInfoDto{SENSITIVE_DATA_REDACTED}";
    }
}

