package com.dumensel.payment.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Masterpass Token Generation Request DTO
 * 
 * Confluence: Masterpass Integration - Token Oluşturma
 * Endpoint: POST /payment/v2/masterpass-payments/generate-token
 * 
 * CRITICAL: Token MUST be generated from Craftgate, NOT from client SDK!
 * 
 * Flow:
 * 1. Merchant Backend → Craftgate: Token generation request
 * 2. Craftgate → Masterpass API: Creates token with POS routing
 * 3. Craftgate → Merchant: Returns token, referenceId, orderNo, terminalGroupId
 * 4. Merchant → Client: Pass token to MFS.purchase()
 */
public class MasterpassTokenRequest {

    /**
     * Phone number (MSISDN) - MANDATORY
     * Format: 905XXXXXXXXX (without +90)
     * 
     * SECURITY: Bu alan ASLA loglanmamalı (PII data)
     */
    @NotBlank(message = "MSISDN is required for Masterpass")
    @Pattern(regexp = "^905\\d{9}$", message = "Invalid Turkish mobile number format")
    private String msisdn;

    /**
     * User ID - MANDATORY
     * Unique identifier for the user
     * Convention: CG_{msisdn} or custom user ID
     */
    @NotBlank(message = "User ID is required")
    private String userId;

    /**
     * BIN Number (First 6 digits of card)
     * Used for POS routing and loyalty check
     */
    @NotBlank(message = "BIN number is required")
    @Pattern(regexp = "^\\d{6}$", message = "BIN must be 6 digits")
    private String binNumber;

    /**
     * Payment amount
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    /**
     * Currency code
     */
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "TRY|USD|EUR|GBP", message = "Invalid currency")
    private String currency;

    /**
     * Installment count
     * Default: 1 (single payment)
     */
    @Min(value = 1, message = "Installment must be at least 1")
    private Integer installment = 1;

    /**
     * Conversation ID for idempotency
     */
    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    /**
     * Buyer ID (optional)
     */
    private String buyerId;

    /**
     * Force 3DS
     * Default: false (Masterpass decides based on card/amount)
     */
    private Boolean forceThreeDS = false;

    // Getters and Setters
    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
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

    public Integer getInstallment() {
        return installment;
    }

    public void setInstallment(Integer installment) {
        this.installment = installment;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public Boolean getForceThreeDS() {
        return forceThreeDS;
    }

    public void setForceThreeDS(Boolean forceThreeDS) {
        this.forceThreeDS = forceThreeDS;
    }
}
