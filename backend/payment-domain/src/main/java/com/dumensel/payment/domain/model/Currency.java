package com.dumensel.payment.domain.model;

/**
 * Value Object for Currency
 */
public enum Currency {
    TRY("Turkish Lira", "₺"),
    USD("US Dollar", "$"),
    EUR("Euro", "€"),
    GBP("British Pound", "£");

    private final String displayName;
    private final String symbol;

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}

