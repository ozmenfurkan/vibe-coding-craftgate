package com.dumensel.payment.domain.model;

/**
 * Payment Status Enum
 */
public enum PaymentStatus {
    PENDING("Payment is pending"),
    PROCESSING("Payment is being processed"),
    SUCCESS("Payment successful"),
    FAILED("Payment failed"),
    CANCELLED("Payment cancelled"),
    REFUNDED("Payment refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFinal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == REFUNDED;
    }
}

