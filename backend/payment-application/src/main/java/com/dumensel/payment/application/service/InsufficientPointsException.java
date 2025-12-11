package com.dumensel.payment.application.service;

/**
 * Exception thrown when user has insufficient points
 * Application layer exception
 */
public class InsufficientPointsException extends RuntimeException {
    
    public InsufficientPointsException(String message) {
        super(message);
    }
    
    public InsufficientPointsException(String message, Throwable cause) {
        super(message, cause);
    }
}
