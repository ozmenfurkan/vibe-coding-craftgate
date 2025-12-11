package com.dumensel.payment.infrastructure.gateway.akbank;

import com.dumensel.payment.application.gateway.PaymentGateway;
import com.dumensel.payment.application.service.PaymentGatewayException;
import com.dumensel.payment.domain.model.Payment;
import com.dumensel.payment.infrastructure.gateway.akbank.model.AkbankPaymentRequest;
import com.dumensel.payment.infrastructure.gateway.akbank.model.AkbankPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

/**
 * Akbank Sanal POS Payment Gateway Implementation
 * GERÇEK API entegrasyonu
 * 
 * Akbank API Documentation: docs/AKBANK_SANAL_POS_ENTEGRASYON_DOKÜMANI_V3_1.pdf
 */
@Component
@ConditionalOnProperty(name = "akbank.enabled", havingValue = "true")
public class AkbankPaymentGateway implements PaymentGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(AkbankPaymentGateway.class);
    
    private final String clientId;
    private final String storeKey;
    private final String apiUrl;
    private final String successUrl;
    private final String failureUrl;
    private final String callbackUrl;
    private final RestTemplate restTemplate;

    public AkbankPaymentGateway(
            @Value("${akbank.client-id}") String clientId,
            @Value("${akbank.store-key}") String storeKey,
            @Value("${akbank.api-url:https://sanalpos.akbank.com.tr/fim/api}") String apiUrl,
            @Value("${akbank.success-url:https://yourdomain.com/payment/success}") String successUrl,
            @Value("${akbank.failure-url:https://yourdomain.com/payment/failure}") String failureUrl,
            @Value("${akbank.callback-url:https://yourdomain.com/payment/callback}") String callbackUrl) {
        
        this.clientId = clientId;
        this.storeKey = storeKey;
        this.apiUrl = apiUrl;
        this.successUrl = successUrl;
        this.failureUrl = failureUrl;
        this.callbackUrl = callbackUrl;
        this.restTemplate = new RestTemplate();
        
        logger.info("Akbank Payment Gateway initialized. ClientId: {}", clientId);
    }

    @Override
    public String processPayment(Payment payment) {
        try {
            logger.info("Processing Akbank payment. ConversationId: {}, Amount: {}", 
                payment.getConversationId(), payment.getAmount().getAmount());
            
            // Akbank request oluştur
            AkbankPaymentRequest request = buildAkbankRequest(payment);
            
            // Hash hesapla (Akbank güvenlik)
            String hash = calculateHash(request);
            request.setHash(hash);
            
            // Akbank API'sine POST çağrısı
            AkbankPaymentResponse response = sendPaymentRequest(request);
            
            // Response validation
            validateResponse(response);
            
            // 3D Secure kontrolü
            if (!response.is3DSecureSuccess()) {
                throw new PaymentGatewayException(
                    "AKBANK_3D_SECURE_FAILED",
                    "3D Secure validation failed. Status: " + response.getMdStatus()
                );
            }
            
            // Ödeme onaylandı mı?
            if (!response.isApproved()) {
                throw new PaymentGatewayException(
                    "AKBANK_PAYMENT_DECLINED",
                    "Payment declined. Code: " + response.getProcReturnCode() + 
                    ", Message: " + response.getErrorMessage()
                );
            }
            
            logger.info("Akbank payment successful. ConversationId: {}, TransactionId: {}, AuthCode: {}", 
                payment.getConversationId(), response.getTransactionId(), response.getAuthCode());
            
            return response.getTransactionId();
            
        } catch (PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Akbank payment failed for conversationId: {}. Error: {}", 
                payment.getConversationId(), e.getMessage(), e);
            
            throw new PaymentGatewayException(
                "AKBANK_TECHNICAL_ERROR",
                "Technical error during Akbank payment: " + e.getMessage(),
                e
            );
        }
    }

    @Override
    public String checkPaymentStatus(String externalPaymentId) {
        try {
            logger.info("Checking Akbank payment status: {}", externalPaymentId);
            
            // Akbank status check endpoint'i
            String statusUrl = apiUrl + "/status";
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("clientId", clientId);
            params.add("transId", externalPaymentId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = 
                new HttpEntity<>(params, headers);
            
            ResponseEntity<AkbankPaymentResponse> response = restTemplate.exchange(
                statusUrl,
                HttpMethod.POST,
                requestEntity,
                AkbankPaymentResponse.class
            );
            
            if (response.getBody() != null && response.getBody().isApproved()) {
                return "SUCCESS";
            }
            
            return "FAILED";
            
        } catch (Exception e) {
            logger.error("Failed to check Akbank payment status: {}", externalPaymentId, e);
            throw new PaymentGatewayException(
                "AKBANK_STATUS_CHECK_FAILED",
                "Failed to check payment status: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Akbank request builder
     */
    private AkbankPaymentRequest buildAkbankRequest(Payment payment) {
        AkbankPaymentRequest request = new AkbankPaymentRequest();
        
        // Temel bilgiler
        request.setClientId(clientId);
        request.setOrderId(payment.getConversationId());
        request.setAmount(formatAmount(payment.getAmount().getAmount().toString()));
        request.setCurrency(mapCurrencyCode(payment.getAmount().getCurrency()));
        request.setRandom(UUID.randomUUID().toString());
        
        // Transaction type
        request.setTransactionType("Auth"); // Direct auth
        request.setStoreType("3d_pay_hosting"); // 3D Secure with payment
        request.setLanguage("tr");
        
        // URLs
        request.setSuccessUrl(successUrl);
        request.setFailureUrl(failureUrl);
        request.setCallbackUrl(callbackUrl);
        
        // Kart bilgileri
        if (payment.getPaymentMethod().getCardInfo() != null) {
            request.setCardNumber(payment.getPaymentMethod().getCardInfo().getCardNumber());
            request.setExpireMonth(payment.getPaymentMethod().getCardInfo().getExpireMonth());
            request.setExpireYear(payment.getPaymentMethod().getCardInfo().getExpireYear());
            request.setCvv(payment.getPaymentMethod().getCardInfo().getCvv());
            request.setCardHolderName(payment.getPaymentMethod().getCardInfo().getCardHolderName());
        }
        
        return request;
    }

    /**
     * Akbank hash calculation
     * Format: BASE64(SHA512(clientId|oid|amount|okUrl|failUrl|tranType|rnd|storeKey))
     */
    private String calculateHash(AkbankPaymentRequest request) {
        try {
            String hashData = String.join("|",
                request.getClientId(),
                request.getOrderId(),
                request.getAmount(),
                request.getSuccessUrl(),
                request.getFailureUrl(),
                request.getTransactionType(),
                request.getRandom(),
                storeKey
            );
            
            logger.debug("Calculating Akbank hash for orderId: {}", request.getOrderId());
            
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(hashData.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(hashBytes);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate Akbank hash", e);
        }
    }

    /**
     * Send payment request to Akbank API
     */
    private AkbankPaymentResponse sendPaymentRequest(AkbankPaymentRequest request) {
        try {
            String paymentUrl = apiUrl + "/v1/gateway";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Convert to form data
            MultiValueMap<String, String> formData = convertToFormData(request);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = 
                new HttpEntity<>(formData, headers);
            
            logger.debug("Sending Akbank payment request to: {}", paymentUrl);
            
            ResponseEntity<AkbankPaymentResponse> response = restTemplate.exchange(
                paymentUrl,
                HttpMethod.POST,
                requestEntity,
                AkbankPaymentResponse.class
            );
            
            return response.getBody();
            
        } catch (RestClientException e) {
            logger.error("Failed to send request to Akbank API", e);
            throw new PaymentGatewayException(
                "AKBANK_API_ERROR",
                "Failed to communicate with Akbank API: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Convert request object to form data
     */
    private MultiValueMap<String, String> convertToFormData(AkbankPaymentRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        
        formData.add("clientId", request.getClientId());
        formData.add("amount", request.getAmount());
        formData.add("oid", request.getOrderId());
        formData.add("okUrl", request.getSuccessUrl());
        formData.add("failUrl", request.getFailureUrl());
        formData.add("callbackUrl", request.getCallbackUrl());
        formData.add("trantype", request.getTransactionType());
        formData.add("currency", request.getCurrency());
        formData.add("rnd", request.getRandom());
        formData.add("hash", request.getHash());
        formData.add("storetype", request.getStoreType());
        formData.add("lang", request.getLanguage());
        
        // Kart bilgileri
        if (request.getCardNumber() != null) {
            formData.add("pan", request.getCardNumber());
            formData.add("Ecom_Payment_Card_ExpDate_Month", request.getExpireMonth());
            formData.add("Ecom_Payment_Card_ExpDate_Year", request.getExpireYear());
            formData.add("cv2", request.getCvv());
            formData.add("cardHolderName", request.getCardHolderName());
        }
        
        return formData;
    }

    /**
     * Validate Akbank response
     */
    private void validateResponse(AkbankPaymentResponse response) {
        if (response == null) {
            throw new PaymentGatewayException(
                "AKBANK_EMPTY_RESPONSE",
                "Empty response from Akbank API"
            );
        }
        
        // Hash validation
        if (response.getHash() != null) {
            // TODO: Validate response hash
            // String expectedHash = calculateResponseHash(response);
            // if (!expectedHash.equals(response.getHash())) {
            //     throw new PaymentGatewayException("AKBANK_HASH_MISMATCH", "Response hash validation failed");
            // }
        }
    }

    /**
     * Format amount for Akbank (e.g., 100.50 → 100.50)
     */
    private String formatAmount(String amount) {
        return amount;
    }

    /**
     * Map currency to Akbank currency code
     * 949 = TRY, 840 = USD, 978 = EUR, 826 = GBP
     */
    private String mapCurrencyCode(com.dumensel.payment.domain.model.Currency currency) {
        return switch (currency) {
            case TRY -> "949";
            case USD -> "840";
            case EUR -> "978";
            case GBP -> "826";
        };
    }
}
