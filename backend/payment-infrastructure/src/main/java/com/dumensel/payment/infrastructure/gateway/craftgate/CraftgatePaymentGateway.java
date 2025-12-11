package com.dumensel.payment.infrastructure.gateway.craftgate;

import com.dumensel.payment.application.gateway.PaymentGateway;
import com.dumensel.payment.application.service.PaymentGatewayException;
import com.dumensel.payment.domain.model.Payment;
import io.craftgate.Craftgate;
import io.craftgate.model.*;
import io.craftgate.request.CreatePaymentRequest;
import io.craftgate.request.dto.PaymentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Craftgate Payment Gateway Implementation
 * Infrastructure layer - External API integration
 */
@Component
public class CraftgatePaymentGateway implements PaymentGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(CraftgatePaymentGateway.class);
    
    private final Craftgate craftgate;

    public CraftgatePaymentGateway(
            @Value("${craftgate.api-key}") String apiKey,
            @Value("${craftgate.secret-key}") String secretKey,
            @Value("${craftgate.base-url:https://sandbox-api.craftgate.io}") String baseUrl) {
        
        this.craftgate = new Craftgate(apiKey, secretKey, baseUrl);
        logger.info("Craftgate client initialized with base URL: {}", baseUrl);
    }

    @Override
    public String processPayment(Payment payment) {
        try {
            logger.info("Processing payment with conversationId: {}", payment.getConversationId());
            
            // Craftgate request oluştur
            CreatePaymentRequest request = buildCraftgateRequest(payment);
            
            // Craftgate API çağrısı
            io.craftgate.response.PaymentResponse response = craftgate.payment().createPayment(request);
            
            // CRITICAL: Hassas kart bilgileri loglanmamalı!
            logger.info("Payment processed successfully. ConversationId: {}, PaymentId: {}", 
                payment.getConversationId(), response.getId());
            
            return String.valueOf(response.getId());
            
        } catch (Exception e) {
            // Craftgate hata yönetimi
            logger.error("Payment failed for conversationId: {}. Error: {}", 
                payment.getConversationId(), e.getMessage());
            
            String errorCode = extractErrorCode(e);
            String errorMessage = e.getMessage();
            
            throw new PaymentGatewayException(errorCode, errorMessage, e);
        }
    }

    @Override
    public String checkPaymentStatus(String externalPaymentId) {
        try {
            io.craftgate.response.PaymentResponse response = 
                craftgate.payment().retrievePayment(Long.parseLong(externalPaymentId));
            
            return response.getPaymentStatus().name();
            
        } catch (Exception e) {
            logger.error("Failed to check payment status for externalId: {}", externalPaymentId, e);
            throw new PaymentGatewayException("STATUS_CHECK_FAILED", e.getMessage(), e);
        }
    }

    private CreatePaymentRequest buildCraftgateRequest(Payment payment) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        
        // Temel bilgiler
        request.setConversationId(payment.getConversationId());
        request.setPrice(payment.getAmount().getAmount());
        request.setPaidPrice(payment.getAmount().getAmount());
        request.setCurrency(mapCurrency(payment.getAmount().getCurrency()));
        request.setInstallment(1); // Tek çekim
        
        // Kart bilgileri
        io.craftgate.request.dto.Card card = new io.craftgate.request.dto.Card();
        card.setCardHolderName(payment.getPaymentMethod().getCardInfo().getCardHolderName());
        card.setCardNumber(payment.getPaymentMethod().getCardInfo().getCardNumber());
        card.setExpireMonth(payment.getPaymentMethod().getCardInfo().getExpireMonth());
        card.setExpireYear(payment.getPaymentMethod().getCardInfo().getExpireYear());
        card.setCvc(payment.getPaymentMethod().getCardInfo().getCvv());
        request.setCard(card);
        
        // Ürün bilgisi (zorunlu)
        PaymentItem item = new PaymentItem();
        item.setName("Payment");
        item.setPrice(payment.getAmount().getAmount());
        request.setItems(Collections.singletonList(item));
        
        return request;
    }

    private io.craftgate.model.Currency mapCurrency(com.dumensel.payment.domain.model.Currency currency) {
        return switch (currency) {
            case TRY -> io.craftgate.model.Currency.TRY;
            case USD -> io.craftgate.model.Currency.USD;
            case EUR -> io.craftgate.model.Currency.EUR;
            case GBP -> io.craftgate.model.Currency.GBP;
        };
    }

    private String extractErrorCode(Exception e) {
        // Craftgate exception'larından error code çıkar
        if (e.getMessage() != null && e.getMessage().contains("errorCode")) {
            // Parse error code from message
            return "CRAFTGATE_ERROR";
        }
        return "UNKNOWN_ERROR";
    }
}

