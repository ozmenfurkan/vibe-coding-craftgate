package com.dumensel.payment.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Masterpass Complete Payment Request DTO
 * 
 * Confluence: Masterpass Integration - Complete Payment
 * Endpoint: POST /payment/v2/masterpass-payments/complete
 * 
 * Flow:
 * - responseCode = 0000: No verification → complete directly
 * - responseCode = 5001: OTP verified → complete with new token
 * 
 * This request is called after:
 * 1. MFS.purchase() returns responseCode 0000 or 5001
 * 2. For 5001: After OTP verification, new token is generated
 * 3. Backend calls this endpoint to finalize payment
 */
public class MasterpassCompleteRequest {

    /**
     * Reference ID from token generation response
     * Links this payment to the generated token
     */
    @NotBlank(message = "Reference ID is required")
    private String referenceId;

    /**
     * Conversation ID for tracking
     */
    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    /**
     * New token (if OTP was verified)
     * For responseCode 0000: Use original token
     * For responseCode 5001: Use token after OTP verification
     */
    private String token;

    // Constructor
    public MasterpassCompleteRequest() {
    }

    public MasterpassCompleteRequest(String referenceId, String conversationId, String token) {
        this.referenceId = referenceId;
        this.conversationId = conversationId;
        this.token = token;
    }

    // Getters and Setters
    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
