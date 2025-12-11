package com.dumensel.payment.application.service;

import com.dumensel.payment.application.dto.*;
import com.dumensel.payment.application.gateway.PaymentGateway;
import com.dumensel.payment.application.gateway.PaymentGatewayFactory;
import com.dumensel.payment.domain.model.*;
import com.dumensel.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Payment Application Service
 * Orchestrates use cases and handles transactions
 */
@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory gatewayFactory;

    public PaymentService(PaymentRepository paymentRepository, 
                         PaymentGatewayFactory gatewayFactory) {
        this.paymentRepository = paymentRepository;
        this.gatewayFactory = gatewayFactory;
    }

    /**
     * Ödeme oluşturma use case
     * İdempotency: Aynı conversationId ile gelen istekler için aynı sonuç döner
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // İdempotency kontrolü: Daha önce bu conversationId ile ödeme yapılmış mı?
        Optional<Payment> existingPayment = paymentRepository
            .findByConversationId(request.getConversationId());
        
        if (existingPayment.isPresent()) {
            // Aynı istek daha önce yapılmış, mevcut sonucu dön
            return mapToResponse(existingPayment.get());
        }

        // DTO'dan Domain Model'e dönüşüm
        Payment payment = createPaymentFromRequest(request);
        
        // Ödemeyi veritabanına kaydet (PENDING durumunda)
        payment = paymentRepository.save(payment);
        
        try {
            // Provider'a göre doğru gateway'i seç
            PaymentGateway gateway = gatewayFactory.getGateway(payment.getProvider());
            
            // Payment Gateway'e istek gönder
            String externalPaymentId = gateway.processPayment(payment);
            
            // Başarılı ödeme
            payment.markAsSuccess(externalPaymentId);
            payment = paymentRepository.save(payment);
            
        } catch (PaymentGatewayException e) {
            // Ödeme başarısız
            payment.markAsFailed(e.getErrorCode(), e.getMessage());
            payment = paymentRepository.save(payment);
        }
        
        return mapToResponse(payment);
    }

    /**
     * Ödeme sorgulama
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }

    /**
     * ConversationId ile ödeme sorgulama (idempotency için)
     */
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentByConversationId(String conversationId) {
        return paymentRepository.findByConversationId(conversationId)
            .map(this::mapToResponse);
    }

    // DTO <-> Domain dönüşümleri
    private Payment createPaymentFromRequest(CreatePaymentRequest request) {
        Money amount = new Money(
            request.getAmount(),
            Currency.valueOf(request.getCurrency())
        );
        
        CardInfo cardInfo = new CardInfo(
            request.getCardInfo().getCardHolderName(),
            request.getCardInfo().getCardNumber(),
            request.getCardInfo().getExpireMonth(),
            request.getCardInfo().getExpireYear(),
            request.getCardInfo().getCvv()
        );
        
        PaymentMethod paymentMethod = new PaymentMethod(PaymentType.CREDIT_CARD, cardInfo);
        PaymentProvider provider = PaymentProvider.valueOf(request.getProvider());
        
        return new Payment(
            request.getConversationId(),
            amount,
            paymentMethod,
            provider,
            request.getBuyerId()
        );
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setConversationId(payment.getConversationId());
        response.setAmount(payment.getAmount().getAmount());
        response.setCurrency(payment.getAmount().getCurrency().name());
        response.setStatus(payment.getStatus().name());
        response.setProvider(payment.getProvider().name());
        response.setBuyerId(payment.getBuyerId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setExternalPaymentId(payment.getExternalPaymentId());
        response.setErrorMessage(payment.getErrorMessage());
        response.setErrorCode(payment.getErrorCode());
        return response;
    }
}

