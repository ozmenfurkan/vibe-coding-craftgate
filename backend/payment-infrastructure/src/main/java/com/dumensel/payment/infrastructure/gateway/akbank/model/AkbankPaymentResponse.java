package com.dumensel.payment.infrastructure.gateway.akbank.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Akbank Sanal POS Payment Response Model
 */
public class AkbankPaymentResponse {
    
    @JsonProperty("Response")
    private String response; // Approved, Declined, Error
    
    @JsonProperty("ProcReturnCode")
    private String procReturnCode;
    
    @JsonProperty("mdStatus")
    private String mdStatus; // 3D Secure status
    
    @JsonProperty("mdErrorMsg")
    private String mdErrorMessage;
    
    @JsonProperty("ErrMsg")
    private String errorMessage;
    
    @JsonProperty("AuthCode")
    private String authCode;
    
    @JsonProperty("TransId")
    private String transactionId;
    
    @JsonProperty("oid")
    private String orderId;
    
    @JsonProperty("amount")
    private String amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("HASH")
    private String hash;
    
    @JsonProperty("HASHPARAMS")
    private String hashParams;
    
    @JsonProperty("HASHPARAMSVAL")
    private String hashParamsValue;
    
    @JsonProperty("rnd")
    private String random;
    
    @JsonProperty("clientIp")
    private String clientIp;
    
    // Constructors
    public AkbankPaymentResponse() {
    }

    // Helper methods
    public boolean isApproved() {
        return "Approved".equalsIgnoreCase(response);
    }

    public boolean is3DSecureSuccess() {
        return "1".equals(mdStatus) || "2".equals(mdStatus) || 
               "3".equals(mdStatus) || "4".equals(mdStatus);
    }

    // Getters and Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getProcReturnCode() {
        return procReturnCode;
    }

    public void setProcReturnCode(String procReturnCode) {
        this.procReturnCode = procReturnCode;
    }

    public String getMdStatus() {
        return mdStatus;
    }

    public void setMdStatus(String mdStatus) {
        this.mdStatus = mdStatus;
    }

    public String getMdErrorMessage() {
        return mdErrorMessage;
    }

    public void setMdErrorMessage(String mdErrorMessage) {
        this.mdErrorMessage = mdErrorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHashParams() {
        return hashParams;
    }

    public void setHashParams(String hashParams) {
        this.hashParams = hashParams;
    }

    public String getHashParamsValue() {
        return hashParamsValue;
    }

    public void setHashParamsValue(String hashParamsValue) {
        this.hashParamsValue = hashParamsValue;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}

