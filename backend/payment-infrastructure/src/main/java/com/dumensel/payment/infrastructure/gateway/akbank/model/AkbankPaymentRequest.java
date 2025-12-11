package com.dumensel.payment.infrastructure.gateway.akbank.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Akbank Sanal POS Payment Request Model
 * Akbank API specification based
 */
public class AkbankPaymentRequest {
    
    @JsonProperty("clientId")
    private String clientId;
    
    @JsonProperty("amount")
    private String amount;
    
    @JsonProperty("oid")
    private String orderId;
    
    @JsonProperty("okUrl")
    private String successUrl;
    
    @JsonProperty("failUrl")
    private String failureUrl;
    
    @JsonProperty("callbackUrl")
    private String callbackUrl;
    
    @JsonProperty("trantype")
    private String transactionType; // Auth, PreAuth, PostAuth
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("rnd")
    private String random;
    
    @JsonProperty("hash")
    private String hash;
    
    @JsonProperty("storetype")
    private String storeType; // 3d, 3d_pay, 3d_host
    
    @JsonProperty("lang")
    private String language;
    
    // Card information
    @JsonProperty("pan")
    private String cardNumber;
    
    @JsonProperty("Ecom_Payment_Card_ExpDate_Month")
    private String expireMonth;
    
    @JsonProperty("Ecom_Payment_Card_ExpDate_Year")
    private String expireYear;
    
    @JsonProperty("cv2")
    private String cvv;
    
    @JsonProperty("cardHolderName")
    private String cardHolderName;
    
    // Constructors
    public AkbankPaymentRequest() {
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getFailureUrl() {
        return failureUrl;
    }

    public void setFailureUrl(String failureUrl) {
        this.failureUrl = failureUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
}

