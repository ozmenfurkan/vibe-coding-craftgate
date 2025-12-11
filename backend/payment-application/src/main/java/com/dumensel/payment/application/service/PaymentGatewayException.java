package com.dumensel.payment.application.service;

/**
 * Exception thrown when payment gateway operations fail
 */
public class PaymentGatewayException extends RuntimeException {
    private final String errorCode;

    public PaymentGatewayException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PaymentGatewayException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

