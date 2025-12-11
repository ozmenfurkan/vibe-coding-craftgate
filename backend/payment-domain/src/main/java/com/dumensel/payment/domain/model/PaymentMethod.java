package com.dumensel.payment.domain.model;

/**
 * Payment Method Value Object
 * Kart bilgileri ASLA log'lanmamalı (PCI-DSS compliance)
 */
public class PaymentMethod {
    private final PaymentType type;
    private final CardInfo cardInfo; // Nullable for non-card payments

    public PaymentMethod(PaymentType type, CardInfo cardInfo) {
        this.type = type;
        this.cardInfo = cardInfo;
        
        if (type == PaymentType.CREDIT_CARD && cardInfo == null) {
            throw new IllegalArgumentException("Card info required for credit card payments");
        }
    }

    public PaymentType getType() {
        return type;
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    // Güvenlik: toString'de hassas bilgi gösterme
    @Override
    public String toString() {
        return "PaymentMethod{type=" + type + "}";
    }
}

