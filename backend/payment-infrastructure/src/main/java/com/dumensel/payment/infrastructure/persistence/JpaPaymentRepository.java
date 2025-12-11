package com.dumensel.payment.infrastructure.persistence;

import com.dumensel.payment.domain.model.Payment;
import com.dumensel.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Implementation of PaymentRepository
 * Infrastructure layer - persistence adapter
 */
@Repository
public class JpaPaymentRepository implements PaymentRepository {
    
    private final SpringDataPaymentRepository springDataRepository;

    public JpaPaymentRepository(SpringDataPaymentRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentMapper.toEntity(payment);
        PaymentEntity saved = springDataRepository.save(entity);
        return PaymentMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(String id) {
        return springDataRepository.findById(id)
                .map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByConversationId(String conversationId) {
        return springDataRepository.findByConversationId(conversationId)
                .map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByExternalPaymentId(String externalPaymentId) {
        return springDataRepository.findByExternalPaymentId(externalPaymentId)
                .map(PaymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByBuyerId(String buyerId) {
        return springDataRepository.findByBuyerId(buyerId)
                .stream()
                .map(PaymentMapper::toDomain)
                .toList();
    }
}

