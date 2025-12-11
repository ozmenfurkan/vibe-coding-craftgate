package com.dumensel.payment.infrastructure.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Shopify Webhook HMAC Validator
 * Infrastructure Layer - Security implementation
 * 
 * Shopify webhook'larının authenticity'sini doğrular
 * 
 * Shopify webhook'ları X-Shopify-Hmac-SHA256 header'ı ile HMAC imzası içerir.
 * Bu imza, webhook secret key ile request body'nin hash'lenerek oluşturulur.
 * 
 * Security: Bu validation MUTLAKA yapılmalıdır!
 * Aksi takdirde, herhangi biri fake webhook gönderebilir.
 */
@Component
public class ShopifyWebhookValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ShopifyWebhookValidator.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    
    private final String webhookSecret;

    public ShopifyWebhookValidator(@Value("${shopify.webhook-secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    /**
     * Shopify webhook HMAC signature'ını doğrular
     * 
     * @param hmacHeader X-Shopify-Hmac-SHA256 header value
     * @param requestBody Raw webhook request body (JSON string)
     * @return true ise signature valid, false ise invalid (fake webhook!)
     */
    public boolean validateWebhook(String hmacHeader, String requestBody) {
        if (hmacHeader == null || hmacHeader.isEmpty()) {
            logger.error("SECURITY: HMAC header missing in Shopify webhook!");
            return false;
        }
        
        if (requestBody == null || requestBody.isEmpty()) {
            logger.error("SECURITY: Request body empty in Shopify webhook!");
            return false;
        }
        
        try {
            // Expected HMAC hesapla
            String calculatedHmac = calculateHmac(requestBody);
            
            // Shopify'dan gelen HMAC ile karşılaştır
            boolean isValid = hmacHeader.equals(calculatedHmac);
            
            if (!isValid) {
                logger.error("SECURITY ALERT: Invalid Shopify webhook HMAC signature! " +
                    "This might be a fake webhook attack.");
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error validating Shopify webhook HMAC", e);
            return false;
        }
    }

    /**
     * HMAC-SHA256 hash hesaplar
     * 
     * Algorithm:
     * 1. Request body'yi UTF-8 olarak al
     * 2. Webhook secret key ile HMAC-SHA256 hash'le
     * 3. Base64 encode et
     */
    private String calculateHmac(String requestBody) throws NoSuchAlgorithmException, InvalidKeyException {
        // HMAC-SHA256 Mac instance
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            webhookSecret.getBytes(StandardCharsets.UTF_8), 
            HMAC_ALGORITHM
        );
        mac.init(secretKeySpec);
        
        // Hash hesapla
        byte[] hash = mac.doFinal(requestBody.getBytes(StandardCharsets.UTF_8));
        
        // Base64 encode
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Shopify webhook topic'ini validate eder
     * 
     * Sadece belirli topic'lere izin verilir
     * 
     * @param topic X-Shopify-Topic header value (e.g., "orders/create")
     * @return true ise topic destekleniyor
     */
    public boolean isSupportedTopic(String topic) {
        if (topic == null) {
            return false;
        }
        
        // Desteklenen webhook topics
        return switch (topic) {
            case "orders/create" -> true; // Yeni sipariş oluşturuldu
            case "orders/paid" -> true;   // Sipariş ödendi (opsiyonel)
            case "orders/cancelled" -> true; // Sipariş iptal edildi (opsiyonel)
            default -> {
                logger.warn("Unsupported Shopify webhook topic: {}", topic);
                yield false;
            }
        };
    }

    /**
     * Webhook shop domain'ini validate eder
     * 
     * Security: Sadece belirli shop'lardan webhook kabul edilebilir
     * 
     * @param shopDomain X-Shopify-Shop-Domain header value (e.g., "mystore.myshopify.com")
     * @return true ise shop authorized
     */
    public boolean isAuthorizedShop(String shopDomain) {
        // TODO: İleri güvenlik için, authorized shops listesi tutulabilir
        // Örneğin: sadece kendi shop'unuzdan webhook kabul edin
        
        if (shopDomain == null || shopDomain.isEmpty()) {
            logger.error("SECURITY: Shop domain missing in Shopify webhook!");
            return false;
        }
        
        // Basic validation: Shopify domain format
        if (!shopDomain.endsWith(".myshopify.com")) {
            logger.warn("SECURITY: Suspicious shop domain: {}", shopDomain);
            return false;
        }
        
        logger.info("Webhook received from shop: {}", shopDomain);
        return true;
    }
}
