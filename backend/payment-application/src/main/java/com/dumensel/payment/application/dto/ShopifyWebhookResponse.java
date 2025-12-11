package com.dumensel.payment.application.dto;

/**
 * Shopify Webhook Response DTO
 * Webhook işleme sonucunu döner
 */
public class ShopifyWebhookResponse {
    
    private boolean success;
    private String message;
    private String paymentId; // Oluşturulan payment ID (varsa)
    private String errorCode; // Hata kodu (varsa)

    public ShopifyWebhookResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ShopifyWebhookResponse(boolean success, String message, String paymentId) {
        this.success = success;
        this.message = message;
        this.paymentId = paymentId;
    }

    public static ShopifyWebhookResponse success(String paymentId) {
        return new ShopifyWebhookResponse(true, "Payment processed successfully", paymentId);
    }

    public static ShopifyWebhookResponse error(String errorCode, String message) {
        ShopifyWebhookResponse response = new ShopifyWebhookResponse(false, message);
        response.setErrorCode(errorCode);
        return response;
    }

    // Getters & Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
