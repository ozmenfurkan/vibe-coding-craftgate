package com.dumensel.payment.infrastructure.gateway.masterpass;

import com.dumensel.payment.application.gateway.PaymentGateway;
import com.dumensel.payment.application.service.PaymentGatewayException;
import com.dumensel.payment.domain.model.Payment;
import io.craftgate.Craftgate;
import io.craftgate.request.CreatePaymentRequest;
import io.craftgate.request.dto.Card;
import io.craftgate.request.dto.PaymentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Masterpass Payment Gateway Implementation (V2)
 * 
 * Confluence: Masterpass Integration
 * - V2 uses server-side integration with Craftgate
 * - Client-side SDK handles UI (card linking, OTP, etc.)
 * - Token must be generated via Craftgate (NOT client SDK)
 * - Supports Non-3DS (OTP) and 3DS flows
 * 
 * CRITICAL Security Rules:
 * - NEVER log msisdn (phone number)
 * - NEVER log card information
 * - Token generation requires proper POS routing
 */
@Component
public class MasterpassPaymentGateway implements PaymentGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(MasterpassPaymentGateway.class);
    
    private final Craftgate craftgate;

    public MasterpassPaymentGateway(
            @Value("${masterpass.api-key}") String apiKey,
            @Value("${masterpass.secret-key}") String secretKey,
            @Value("${masterpass.base-url:https://sandbox-api.craftgate.io}") String baseUrl) {
        
        this.craftgate = new Craftgate(apiKey, secretKey, baseUrl);
        logger.info("Masterpass gateway initialized with base URL: {}", baseUrl);
    }

    /**
     * Process Masterpass payment (Complete flow)
     * 
     * Flow (from Confluence):
     * 1. Token generated via /masterpass-payments/generate-token (done separately)
     * 2. Client SDK: MFS.purchase() called with token
     * 3. Based on responseCode:
     *    - 0000: No verification needed → complete directly
     *    - 5001: OTP required → verify OTP → complete
     *    - 5010: 3DS required → 3ds-init → 3ds-complete
     * 
     * NOTE: Masterpass V2 uses standard Craftgate payment API
     * The actual Masterpass flow is handled via separate REST endpoints
     * This gateway is for backend payment processing
     */
    @Override
    public String processPayment(Payment payment) {
        try {
            logger.info("Processing Masterpass payment. ConversationId: {}", 
                payment.getConversationId());
            
            // Masterpass V2 uses standard Craftgate payment API
            // Token generation and MFS.purchase() are handled separately via REST API
            CreatePaymentRequest request = buildMasterpassPaymentRequest(payment);
            
            // Craftgate standard payment API (works for Masterpass too)
            io.craftgate.response.PaymentResponse response = 
                craftgate.payment().createPayment(request);
            
            // CRITICAL: NEVER log phone number or card info
            logger.info("Masterpass payment completed. ConversationId: {}, PaymentId: {}", 
                payment.getConversationId(), response.getId());
            
            return String.valueOf(response.getId());
            
        } catch (Exception e) {
            logger.error("Masterpass payment failed. ConversationId: {}. Error: {}", 
                payment.getConversationId(), e.getMessage());
            
            String errorCode = extractMasterpassErrorCode(e);
            throw new PaymentGatewayException(errorCode, e.getMessage(), e);
        }
    }

    @Override
    public String checkPaymentStatus(String externalPaymentId) {
        try {
            io.craftgate.response.PaymentResponse response = 
                craftgate.payment().retrievePayment(Long.parseLong(externalPaymentId));
            
            return response.getPaymentStatus().name();
            
        } catch (Exception e) {
            logger.error("Failed to check Masterpass payment status. ExternalId: {}", 
                externalPaymentId, e);
            throw new PaymentGatewayException("STATUS_CHECK_FAILED", e.getMessage(), e);
        }
    }

    /**
     * Build Masterpass payment request
     * 
     * Confluence Notes:
     * - Masterpass V2 uses standard Craftgate payment API
     * - price and paidPrice should be same (no loyalty in V2 standard flow)
     * - currency mapping required
     * - items array is mandatory
     * - Actual Masterpass token flow is handled via separate REST endpoints
     */
    private CreatePaymentRequest buildMasterpassPaymentRequest(Payment payment) {
        CreatePaymentRequest request = CreatePaymentRequest.builder().build();
        
        // Basic payment info
        request.setConversationId(payment.getConversationId());
        request.setPrice(payment.getAmount().getAmount());
        request.setPaidPrice(payment.getAmount().getAmount());
        request.setCurrency(mapCurrency(payment.getAmount().getCurrency()));
        request.setInstallment(1); // Masterpass default: single payment
        
        // Payment items (mandatory)
        PaymentItem item = PaymentItem.builder().build();
        item.setName("Payment via Masterpass");
        item.setPrice(payment.getAmount().getAmount());
        request.setItems(Collections.singletonList(item));
        
        // Buyer info (optional but recommended)
        if (payment.getBuyerId() != null) {
            request.setBuyerMemberId(Long.valueOf(payment.getBuyerId()));
        }
        
        // Card info (from Payment domain model)
        if (payment.getPaymentMethod() != null && payment.getPaymentMethod().getCardInfo() != null) {
            io.craftgate.request.dto.Card card = Card.builder().build();
            card.setCardHolderName(payment.getPaymentMethod().getCardInfo().getCardHolderName());
            card.setCardNumber(payment.getPaymentMethod().getCardInfo().getCardNumber());
            card.setExpireMonth(payment.getPaymentMethod().getCardInfo().getExpireMonth());
            card.setExpireYear(payment.getPaymentMethod().getCardInfo().getExpireYear());
            card.setCvc(payment.getPaymentMethod().getCardInfo().getCvv());
            request.setCard(card);
        }
        
        return request;
    }

    /**
     * Currency mapping
     * Confluence: Masterpass supports TRY, USD, EUR, GBP
     */
    private io.craftgate.model.Currency mapCurrency(com.dumensel.payment.domain.model.Currency currency) {
        return switch (currency) {
            case TRY -> io.craftgate.model.Currency.TRY;
            case USD -> io.craftgate.model.Currency.USD;
            case EUR -> io.craftgate.model.Currency.EUR;
            case GBP -> io.craftgate.model.Currency.GBP;
        };
    }

    /**
     * Extract error code from Masterpass exceptions
     * 
     * Common Masterpass errors (from Confluence):
     * - responseCode 193: No response from bank (triggers no-response mechanism)
     * - responseCode 5001: OTP required
     * - responseCode 5010: 3DS required
     * - Error 4057: Order number mismatch
     */
    private String extractMasterpassErrorCode(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "MASTERPASS_UNKNOWN_ERROR";
        }
        
        // Masterpass specific error codes
        if (message.contains("193")) {
            return "MASTERPASS_NO_RESPONSE"; // Triggers no-response job
        } else if (message.contains("5001")) {
            return "MASTERPASS_OTP_REQUIRED";
        } else if (message.contains("5010")) {
            return "MASTERPASS_3DS_REQUIRED";
        } else if (message.contains("4057")) {
            return "MASTERPASS_ORDER_MISMATCH";
        }
        
        return "MASTERPASS_ERROR";
    }
}
