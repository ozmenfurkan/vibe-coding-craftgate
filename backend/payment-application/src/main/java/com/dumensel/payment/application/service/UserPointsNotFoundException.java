package com.dumensel.payment.application.service;

/**
 * Exception thrown when user points are not found
 * Application layer exception
 */
public class UserPointsNotFoundException extends RuntimeException {
    
    public UserPointsNotFoundException(String message) {
        super(message);
    }
    
    public UserPointsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
