package com.dumensel.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository
 * Infrastructure layer
 */
public interface SpringDataPaymentRepository extends JpaRepository<PaymentEntity, String> {
    
    Optional<PaymentEntity> findByConversationId(String conversationId);
    
    Optional<PaymentEntity> findByExternalPaymentId(String externalPaymentId);
    
    List<PaymentEntity> findByBuyerId(String buyerId);
}

