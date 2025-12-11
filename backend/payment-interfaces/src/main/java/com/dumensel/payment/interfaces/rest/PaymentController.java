package com.dumensel.payment.interfaces.rest;

import com.dumensel.payment.application.dto.CreatePaymentRequest;
import com.dumensel.payment.application.dto.PaymentResponse;
import com.dumensel.payment.application.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Payment REST Controller
 * Interface layer - API endpoints
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create a new payment
     * İdempotency-Key header ile idempotency sağlanır
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        // İdempotency-Key kontrolü
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            logger.warn("Payment request without Idempotency-Key. ConversationId: {}", 
                request.getConversationId());
        }
        
        // CRITICAL: Hassas bilgileri loglama!
        logger.info("Creating payment. ConversationId: {}, BuyerId: {}", 
            request.getConversationId(), request.getBuyerId());
        
        PaymentResponse response = paymentService.createPayment(request);
        
        HttpStatus status = response.getStatus().equals("SUCCESS") 
            ? HttpStatus.CREATED 
            : HttpStatus.BAD_REQUEST;
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
        logger.info("Retrieving payment: {}", paymentId);
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by conversation ID (for idempotency check)
     */
    @GetMapping("/by-conversation/{conversationId}")
    public ResponseEntity<PaymentResponse> getPaymentByConversationId(
            @PathVariable String conversationId) {
        logger.info("Retrieving payment by conversationId: {}", conversationId);
        return paymentService.getPaymentByConversationId(conversationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment service is running");
    }
}

