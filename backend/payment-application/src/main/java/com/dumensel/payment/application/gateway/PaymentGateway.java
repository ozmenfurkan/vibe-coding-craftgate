package com.dumensel.payment.application.gateway;

import com.dumensel.payment.domain.model.Payment;

/**
 * Payment Gateway Interface
 * Infrastructure layer - abstraction for external payment providers
 */
public interface PaymentGateway {
    
    /**
     * Process payment through external gateway
     * @param payment Domain payment object
     * @return External payment ID from the gateway
     * @throws com.dumensel.payment.application.service.PaymentGatewayException if payment fails
     */
    String processPayment(Payment payment);
    
    /**
     * Check payment status from external gateway
     * @param externalPaymentId External payment ID
     * @return Payment status from gateway
     */
    String checkPaymentStatus(String externalPaymentId);
}

