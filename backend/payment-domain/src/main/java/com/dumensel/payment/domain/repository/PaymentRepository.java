package com.dumensel.payment.domain.repository;

import com.dumensel.payment.domain.model.Payment;
import java.util.Optional;

/**
 * Payment Repository Interface (Domain Layer)
 * Pure interface, NO Spring annotations
 * Infrastructure layer will implement this
 */
public interface PaymentRepository {
    
    /**
     * Persist a new payment
     */
    Payment save(Payment payment);
    
    /**
     * Find payment by internal ID
     */
    Optional<Payment> findById(String id);
    
    /**
     * Find payment by conversation ID (for idempotency check)
     * İdempotency: Aynı conversationId ile tekrar istek gelirse mevcut payment dönülür
     */
    Optional<Payment> findByConversationId(String conversationId);
    
    /**
     * Find payment by external payment ID (Craftgate ID)
     */
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);
    
    /**
     * Find payments by buyer ID
     */
    java.util.List<Payment> findByBuyerId(String buyerId);
}

