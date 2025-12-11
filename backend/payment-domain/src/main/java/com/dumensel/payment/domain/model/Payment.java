package com.dumensel.payment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Aggregate Root
 * Domain Layer - NO Spring annotations allowed
 */
public class Payment {
    private String id;
    private String conversationId;
    private Money amount;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private PaymentProvider provider;
    private String buyerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String externalPaymentId; // External gateway payment ID
    private String errorMessage;
    private String errorCode;

    // Business constructor (for creating new payments)
    public Payment(String conversationId, Money amount, PaymentMethod paymentMethod, 
                   PaymentProvider provider, String buyerId) {
        this.id = UUID.randomUUID().toString();
        this.conversationId = conversationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.provider = provider;
        this.buyerId = buyerId;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Reconstruction constructor (for loading from database)
    // Package-private: Only infrastructure layer can use this
    Payment(String id, String conversationId, Money amount, PaymentStatus status,
            PaymentMethod paymentMethod, PaymentProvider provider, String buyerId, LocalDateTime createdAt,
            LocalDateTime updatedAt, String externalPaymentId, String errorMessage, String errorCode) {
        this.id = id;
        this.conversationId = conversationId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.provider = provider;
        this.buyerId = buyerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.externalPaymentId = externalPaymentId;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    // Business logic: Ödeme başarılı olarak işaretlenir
    public void markAsSuccess(String externalPaymentId) {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Payment is already successful");
        }
        this.status = PaymentStatus.SUCCESS;
        this.externalPaymentId = externalPaymentId;
        this.updatedAt = LocalDateTime.now();
    }

    // Business logic: Ödeme başarısız olarak işaretlenir
    public void markAsFailed(String errorCode, String errorMessage) {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot fail a successful payment");
        }
        this.status = PaymentStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }

    // Business logic: İptal kontrolü
    public void cancel() {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot cancel a successful payment. Use refund instead.");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    // İdempotency kontrolü için conversationId kullanılır
    public boolean isSameConversation(String conversationId) {
        return this.conversationId.equals(conversationId);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getExternalPaymentId() {
        return externalPaymentId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

