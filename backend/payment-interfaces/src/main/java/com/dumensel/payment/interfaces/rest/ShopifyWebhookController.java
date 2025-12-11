package com.dumensel.payment.interfaces.rest;

import com.dumensel.payment.application.dto.ShopifyWebhookRequest;
import com.dumensel.payment.application.dto.ShopifyWebhookResponse;
import com.dumensel.payment.application.service.ShopifyWebhookService;
import com.dumensel.payment.infrastructure.webhook.ShopifyWebhookValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Shopify Webhook Controller
 * Interface Layer - REST API endpoint
 * 
 * Shopify'dan gelen webhook'ları alır ve işler
 * 
 * Endpoint: POST /api/v1/webhooks/shopify/orders
 */
@RestController
@RequestMapping("/api/v1/webhooks/shopify")
public class ShopifyWebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(ShopifyWebhookController.class);
    
    private final ShopifyWebhookService webhookService;
    private final ShopifyWebhookValidator webhookValidator;

    public ShopifyWebhookController(
            ShopifyWebhookService webhookService,
            ShopifyWebhookValidator webhookValidator) {
        this.webhookService = webhookService;
        this.webhookValidator = webhookValidator;
    }

    /**
     * Shopify "orders/create" webhook endpoint
     * 
     * Shopify webhook configuration:
     * - URL: https://your-domain.com/api/v1/webhooks/shopify/orders
     * - Format: JSON
     * - API version: 2024-01 (or latest)
     * 
     * Headers gönderilen:
     * - X-Shopify-Topic: orders/create
     * - X-Shopify-Hmac-SHA256: {hmac_signature}
     * - X-Shopify-Shop-Domain: {shop_domain}.myshopify.com
     * - X-Shopify-API-Version: 2024-01
     * 
     * @param hmacHeader HMAC signature for security validation
     * @param topic Webhook topic (should be "orders/create")
     * @param shopDomain Shop domain for authorization
     * @param webhookRequest Shopify order payload
     * @param rawBody Raw request body for HMAC validation
     * @return Webhook processing result
     */
    @PostMapping("/orders")
    public ResponseEntity<ShopifyWebhookResponse> handleOrderWebhook(
            @RequestHeader(value = "X-Shopify-Hmac-SHA256", required = false) String hmacHeader,
            @RequestHeader(value = "X-Shopify-Topic", required = false) String topic,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestBody ShopifyWebhookRequest webhookRequest,
            @RequestBody String rawBody) {
        
        try {
            logger.info("Received Shopify webhook. Topic: {}, Shop: {}", topic, shopDomain);
            
            // SECURITY: HMAC validation - MUTLAKA yapılmalı!
            if (!webhookValidator.validateWebhook(hmacHeader, rawBody)) {
                logger.error("SECURITY ALERT: Invalid HMAC signature for Shopify webhook!");
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ShopifyWebhookResponse.error("INVALID_SIGNATURE", 
                        "Webhook signature validation failed"));
            }
            
            // SECURITY: Topic validation
            if (!webhookValidator.isSupportedTopic(topic)) {
                logger.warn("Unsupported webhook topic: {}", topic);
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ShopifyWebhookResponse.error("UNSUPPORTED_TOPIC", 
                        "Webhook topic is not supported"));
            }
            
            // SECURITY: Shop authorization
            if (!webhookValidator.isAuthorizedShop(shopDomain)) {
                logger.error("SECURITY ALERT: Unauthorized shop domain: {}", shopDomain);
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ShopifyWebhookResponse.error("UNAUTHORIZED_SHOP", 
                        "Shop is not authorized"));
            }
            
            // Business validation
            if (!webhookService.validateOrder(webhookRequest)) {
                logger.error("Order validation failed. OrderId: {}", webhookRequest.getId());
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ShopifyWebhookResponse.error("INVALID_ORDER", 
                        "Order validation failed"));
            }
            
            // Process webhook
            ShopifyWebhookResponse response = webhookService.processOrderWebhook(webhookRequest);
            
            // Return appropriate HTTP status based on result
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error processing Shopify webhook", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ShopifyWebhookResponse.error("INTERNAL_ERROR", 
                    "An unexpected error occurred"));
        }
    }

    /**
     * Health check endpoint for Shopify webhook
     * 
     * Shopify webhook'larını test etmek için basit bir endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Shopify webhook endpoint is healthy");
    }

    /**
     * Shopify "orders/paid" webhook endpoint (opsiyonel)
     * 
     * Sipariş ödendiğinde Shopify bu webhook'u tetikler
     * Bu endpoint ile ödeme confirmation yapılabilir
     */
    @PostMapping("/orders/paid")
    public ResponseEntity<ShopifyWebhookResponse> handleOrderPaidWebhook(
            @RequestHeader(value = "X-Shopify-Hmac-SHA256", required = false) String hmacHeader,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestBody ShopifyWebhookRequest webhookRequest,
            @RequestBody String rawBody) {
        
        logger.info("Received orders/paid webhook. OrderId: {}", webhookRequest.getId());
        
        // HMAC validation
        if (!webhookValidator.validateWebhook(hmacHeader, rawBody)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ShopifyWebhookResponse.error("INVALID_SIGNATURE", 
                    "Webhook signature validation failed"));
        }
        
        // Shop authorization
        if (!webhookValidator.isAuthorizedShop(shopDomain)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ShopifyWebhookResponse.error("UNAUTHORIZED_SHOP", 
                    "Shop is not authorized"));
        }
        
        // TODO: İleri implementasyon için:
        // - Payment status güncelleme
        // - Notification gönderme
        // - Invoice oluşturma
        
        logger.info("Order paid notification received. OrderId: {}", webhookRequest.getId());
        return ResponseEntity.ok(
            new ShopifyWebhookResponse(true, "Order paid notification received"));
    }

    /**
     * Shopify "orders/cancelled" webhook endpoint (opsiyonel)
     * 
     * Sipariş iptal edildiğinde Shopify bu webhook'u tetikler
     */
    @PostMapping("/orders/cancelled")
    public ResponseEntity<ShopifyWebhookResponse> handleOrderCancelledWebhook(
            @RequestHeader(value = "X-Shopify-Hmac-SHA256", required = false) String hmacHeader,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestBody ShopifyWebhookRequest webhookRequest,
            @RequestBody String rawBody) {
        
        logger.info("Received orders/cancelled webhook. OrderId: {}", webhookRequest.getId());
        
        // HMAC validation
        if (!webhookValidator.validateWebhook(hmacHeader, rawBody)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ShopifyWebhookResponse.error("INVALID_SIGNATURE", 
                    "Webhook signature validation failed"));
        }
        
        // Shop authorization
        if (!webhookValidator.isAuthorizedShop(shopDomain)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ShopifyWebhookResponse.error("UNAUTHORIZED_SHOP", 
                    "Shop is not authorized"));
        }
        
        // TODO: İleri implementasyon için:
        // - Payment refund işlemi
        // - Inventory güncelleme
        // - Customer notification
        
        logger.info("Order cancelled notification received. OrderId: {}", webhookRequest.getId());
        return ResponseEntity.ok(
            new ShopifyWebhookResponse(true, "Order cancellation processed"));
    }
}
