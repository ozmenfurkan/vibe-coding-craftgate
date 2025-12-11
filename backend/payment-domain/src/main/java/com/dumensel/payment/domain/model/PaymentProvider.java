package com.dumensel.payment.domain.model;

/**
 * Payment Gateway Provider Enum
 * Hangi ödeme sağlayıcısı kullanılacak
 */
public enum PaymentProvider {
    CRAFTGATE("Craftgate", "Turkish payment gateway"),
    AKBANK("Akbank Sanal POS", "Turkish bank virtual POS");

    private final String displayName;
    private final String description;

    PaymentProvider(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}

