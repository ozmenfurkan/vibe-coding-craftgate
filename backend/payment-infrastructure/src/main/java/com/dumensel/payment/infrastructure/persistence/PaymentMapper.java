package com.dumensel.payment.infrastructure.persistence;

import com.dumensel.payment.domain.model.*;

/**
 * Mapper between Domain and Persistence models
 * Infrastructure layer
 */
public class PaymentMapper {
    
    public static PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setConversationId(payment.getConversationId());
        entity.setAmount(payment.getAmount().getAmount());
        entity.setCurrency(payment.getAmount().getCurrency().name());
        entity.setStatus(payment.getStatus().name());
        entity.setPaymentType(payment.getPaymentMethod().getType().name());
        entity.setProvider(payment.getProvider().name());
        entity.setBuyerId(payment.getBuyerId());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        entity.setExternalPaymentId(payment.getExternalPaymentId());
        entity.setErrorMessage(payment.getErrorMessage());
        entity.setErrorCode(payment.getErrorCode());
        
        // Sadece maskelenmiş kart numarasını sakla
        if (payment.getPaymentMethod().getCardInfo() != null) {
            entity.setMaskedCardNumber(
                payment.getPaymentMethod().getCardInfo().getMaskedCardNumber()
            );
        }
        
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
        // NOTE: CardInfo domain nesnesini yeniden oluşturamıyoruz çünkü tam kart bilgileri yok
        // Bu yüzden veritabanından okunan Payment'lar için CardInfo null olabilir
        // Bu kabul edilebilir çünkü kart bilgileri sadece ödeme anında gereklidir
        
        Money money = new Money(
            entity.getAmount(),
            Currency.valueOf(entity.getCurrency())
        );
        
        // CardInfo'yu null olarak geçiyoruz - veritabanında tam kart bilgisi yok
        PaymentMethod paymentMethod = new PaymentMethod(
            PaymentType.valueOf(entity.getPaymentType()),
            null // CardInfo veritabanından restore edilemez (güvenlik)
        );
        
        // Reconstruction constructor kullan (veritabanından restore için)
        return new Payment(
            entity.getId(),
            entity.getConversationId(),
            money,
            PaymentStatus.valueOf(entity.getStatus()),
            paymentMethod,
            PaymentProvider.valueOf(entity.getProvider()),
            entity.getBuyerId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getExternalPaymentId(),
            entity.getErrorMessage(),
            entity.getErrorCode()
        );
    }
}

