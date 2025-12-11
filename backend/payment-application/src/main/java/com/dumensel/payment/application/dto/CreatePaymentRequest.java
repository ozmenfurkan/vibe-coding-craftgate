package com.dumensel.payment.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for creating a payment
 * Bu sınıf API katmanından gelir ve validation annotations içerebilir
 */
public class CreatePaymentRequest {
    
    @NotBlank(message = "Conversation ID is required")
    private String conversationId; // Idempotency key
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "TRY|USD|EUR|GBP", message = "Invalid currency")
    private String currency;
    
    @NotBlank(message = "Buyer ID is required")
    private String buyerId;
    
    @NotBlank(message = "Payment provider is required")
    @Pattern(regexp = "CRAFTGATE|AKBANK", message = "Invalid payment provider")
    private String provider;
    
    @NotNull(message = "Card info is required")
    private CardInfoDto cardInfo;
    
    // Constructors
    public CreatePaymentRequest() {
    }

    public CreatePaymentRequest(String conversationId, BigDecimal amount, 
                               String currency, String buyerId, String provider, CardInfoDto cardInfo) {
        this.conversationId = conversationId;
        this.amount = amount;
        this.currency = currency;
        this.buyerId = buyerId;
        this.provider = provider;
        this.cardInfo = cardInfo;
    }

    // Getters and Setters
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public CardInfoDto getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(CardInfoDto cardInfo) {
        this.cardInfo = cardInfo;
    }

    // GÜVENLİK: toString'de hassas bilgi gösterme
    @Override
    public String toString() {
        return "CreatePaymentRequest{" +
                "conversationId='" + conversationId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", buyerId='" + buyerId + '\'' +
                '}';
    }
}

