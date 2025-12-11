package com.dumensel.payment.application.service;

import com.dumensel.payment.application.dto.CreatePaymentRequest;
import com.dumensel.payment.application.dto.PaymentResponse;
import com.dumensel.payment.application.dto.ShopifyWebhookRequest;
import com.dumensel.payment.application.dto.ShopifyWebhookResponse;
import com.dumensel.payment.domain.model.Currency;
import com.dumensel.payment.domain.model.PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Shopify Webhook Service
 * Application Layer - Use Case orchestration
 * 
 * Shopify webhook'larını işler ve Craftgate ile ödeme sürecini yönetir
 */
@Service
public class ShopifyWebhookService {
    
    private static final Logger logger = LoggerFactory.getLogger(ShopifyWebhookService.class);
    
    private final PaymentService paymentService;

    public ShopifyWebhookService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Shopify order webhook'unu işler ve Craftgate ile ödeme yapar
     * 
     * NOT: Shopify webhook'larında genellikle tam kart bilgisi gelmez.
     * Bu implementasyon için, kart bilgilerinin webhook içinde gelmesi beklenmektedir.
     * Alternatif olarak, stored card token kullanılabilir.
     * 
     * @param webhookRequest Shopify'dan gelen sipariş bilgileri
     * @return Webhook işleme sonucu
     */
    public ShopifyWebhookResponse processOrderWebhook(ShopifyWebhookRequest webhookRequest) {
        try {
            // Sipariş bilgilerini logla (CRITICAL: Email ve phone PII data - sadece ID logla)
            logger.info("Processing Shopify webhook. OrderId: {}, OrderNumber: {}, Amount: {}", 
                webhookRequest.getId(), 
                webhookRequest.getOrderNumber(), 
                webhookRequest.getTotalPrice());
            
            // Validasyon: Sipariş zaten ödenmiş mi?
            if ("paid".equalsIgnoreCase(webhookRequest.getFinancialStatus())) {
                logger.warn("Order already paid. OrderId: {}", webhookRequest.getId());
                return ShopifyWebhookResponse.error("ORDER_ALREADY_PAID", 
                    "Order is already marked as paid in Shopify");
            }
            
            // Validasyon: Ödeme detayları var mı?
            if (webhookRequest.getPaymentDetails() == null) {
                logger.error("Payment details missing in webhook. OrderId: {}", webhookRequest.getId());
                return ShopifyWebhookResponse.error("PAYMENT_DETAILS_MISSING", 
                    "Payment details are required to process payment");
            }
            
            // Shopify webhook → Payment domain modeline dönüşüm
            CreatePaymentRequest paymentRequest = mapToPaymentRequest(webhookRequest);
            
            // Craftgate ile ödeme işlemi
            PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);
            
            logger.info("Shopify order payment successful. OrderId: {}, PaymentId: {}", 
                webhookRequest.getId(), paymentResponse.getId());
            
            return ShopifyWebhookResponse.success(paymentResponse.getId());
            
        } catch (PaymentGatewayException e) {
            logger.error("Payment gateway error for Shopify order: {}. Error: {}", 
                webhookRequest.getId(), e.getMessage());
            return ShopifyWebhookResponse.error(e.getErrorCode(), e.getMessage());
            
        } catch (Exception e) {
            logger.error("Unexpected error processing Shopify webhook. OrderId: {}", 
                webhookRequest.getId(), e);
            return ShopifyWebhookResponse.error("INTERNAL_ERROR", 
                "An unexpected error occurred while processing the order");
        }
    }

    /**
     * Shopify webhook verisini Payment request'e dönüştürür
     */
    private CreatePaymentRequest mapToPaymentRequest(ShopifyWebhookRequest webhook) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        
        // Temel sipariş bilgileri
        request.setConversationId("SHOPIFY-" + webhook.getOrderNumber());
        request.setAmount(webhook.getTotalPrice());
        request.setCurrency(mapCurrency(webhook.getCurrency()));
        request.setProvider(PaymentProvider.CRAFTGATE); // Craftgate ile ödeme
        
        // Kart bilgileri mapping
        // NOT: Shopify webhook'larında genellikle tam kart bilgisi gelmez!
        // Bu implementasyon, kart bilgilerinin webhook'ta geldiğini varsayar
        // Gerçek senaryoda, stored card token veya alternative payment method kullanılmalı
        if (webhook.getPaymentDetails() != null) {
            CreatePaymentRequest.CardInfo cardInfo = new CreatePaymentRequest.CardInfo();
            
            ShopifyWebhookRequest.ShopifyPaymentDetails paymentDetails = webhook.getPaymentDetails();
            
            // Cardholder name
            cardInfo.setCardHolderName(paymentDetails.getCreditCardName());
            
            // CRITICAL: Shopify webhook'larında full card number gelmez!
            // Burada example implementation için placeholder kullanıyoruz
            // Gerçek implementasyonda: stored card token veya alternatif yöntem gerekli
            cardInfo.setCardNumber(reconstructCardNumber(paymentDetails));
            
            // Expiry date
            cardInfo.setExpireMonth(String.format("%02d", paymentDetails.getCreditCardExpMonth()));
            cardInfo.setExpireYear(String.valueOf(paymentDetails.getCreditCardExpYear()));
            
            // CVV (Shopify webhook'larında CVV gelmez!)
            // Stored card scenario için gerekli
            cardInfo.setCvv("000"); // Placeholder - stored card durumunda CVV optional olabilir
            
            request.setCardInfo(cardInfo);
        }
        
        return request;
    }

    /**
     * Shopify'dan gelen kısmi kart bilgisini reconstruct eder
     * 
     * CRITICAL: Bu method production'da KULLANILMAMALI!
     * Shopify webhook'larında full card number gelmez.
     * 
     * Gerçek implementasyonda:
     * - Stored card token kullanılmalı
     * - Veya Shopify payment token kullanılmalı
     * - Veya customer'ın stored card'ı database'den alınmalı
     */
    private String reconstructCardNumber(ShopifyWebhookRequest.ShopifyPaymentDetails paymentDetails) {
        // PLACEHOLDER IMPLEMENTATION - NOT FOR PRODUCTION!
        logger.warn("SECURITY WARNING: Attempting to reconstruct card number. " +
            "This should not happen in production. Use stored card tokens instead.");
        
        // Shopify'dan gelen: BIN (first 6) + masked middle + last 4
        // Örnek: "540669" + "•••• •••• ••" + "4242"
        // Ama webhook'ta sadece last 4 gelir genellikle
        
        throw new IllegalStateException(
            "Full card number not available in Shopify webhook. " +
            "Please implement stored card token mechanism.");
    }

    /**
     * Shopify currency string'ini domain Currency enum'ına dönüştürür
     */
    private Currency mapCurrency(String shopifyCurrency) {
        return switch (shopifyCurrency.toUpperCase()) {
            case "TRY" -> Currency.TRY;
            case "USD" -> Currency.USD;
            case "EUR" -> Currency.EUR;
            case "GBP" -> Currency.GBP;
            default -> {
                logger.warn("Unsupported currency: {}. Defaulting to TRY", shopifyCurrency);
                yield Currency.TRY;
            }
        };
    }

    /**
     * Sipariş validation helper
     * İleriki geliştirmeler için:
     * - Minimum/maximum order amount kontrolü
     * - Duplicate order kontrolü
     * - Customer verification
     */
    public boolean validateOrder(ShopifyWebhookRequest webhook) {
        // Basic validation
        if (webhook.getId() == null || webhook.getOrderNumber() == null) {
            logger.error("Invalid webhook: Missing order ID or number");
            return false;
        }
        
        if (webhook.getTotalPrice() == null || webhook.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid webhook: Invalid total price. OrderId: {}", webhook.getId());
            return false;
        }
        
        // İleri validasyonlar burada eklenebilir
        
        return true;
    }
}
