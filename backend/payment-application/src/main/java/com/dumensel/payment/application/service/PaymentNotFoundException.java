package com.dumensel.payment.application.service;

/**
 * Exception thrown when payment is not found
 */
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}

