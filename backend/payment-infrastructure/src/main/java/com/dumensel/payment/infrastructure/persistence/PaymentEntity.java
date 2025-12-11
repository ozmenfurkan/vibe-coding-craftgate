package com.dumensel.payment.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for Payment
 * Infrastructure layer - persistence model
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_conversation_id", columnList = "conversation_id", unique = true),
    @Index(name = "idx_external_payment_id", columnList = "external_payment_id"),
    @Index(name = "idx_buyer_id", columnList = "buyer_id")
})
public class PaymentEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "conversation_id", nullable = false, unique = true, length = 100)
    private String conversationId;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType;
    
    @Column(name = "provider", nullable = false, length = 20)
    private String provider;
    
    @Column(name = "buyer_id", nullable = false, length = 100)
    private String buyerId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "external_payment_id", length = 100)
    private String externalPaymentId;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Column(name = "error_code", length = 50)
    private String errorCode;
    
    // CRITICAL: Kart bilgileri burada saklanmamalı veya şifrelenmeli
    // Production'da PCI-DSS compliance için tokenization kullanılmalı
    @Column(name = "masked_card_number", length = 20)
    private String maskedCardNumber;

    // Constructors
    public PaymentEntity() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getExternalPaymentId() {
        return externalPaymentId;
    }

    public void setExternalPaymentId(String externalPaymentId) {
        this.externalPaymentId = externalPaymentId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }
}

