package com.dumensel.payment.interfaces.rest;

import com.dumensel.payment.application.dto.*;
import com.dumensel.payment.application.service.UserPointsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserPoints REST Controller
 * Interface layer - API endpoints for user points management
 */
@RestController
@RequestMapping("/api/v1/user-points")
public class UserPointsController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserPointsController.class);
    
    private final UserPointsService userPointsService;

    public UserPointsController(UserPointsService userPointsService) {
        this.userPointsService = userPointsService;
    }

    /**
     * Get user points by user ID
     * Kullanıcı puan sorgulama endpoint
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserPointsDto> getUserPoints(@PathVariable String userId) {
        logger.info("Querying points for user: {}", userId);
        
        UserPointsDto userPoints = userPointsService.getUserPoints(userId);
        
        return ResponseEntity.ok(userPoints);
    }

    /**
     * Earn points
     * Kullanıcıya puan ekleme (başarılı ödeme sonrası çağrılır)
     */
    @PostMapping("/earn")
    public ResponseEntity<UserPointsDto> earnPoints(@Valid @RequestBody EarnPointsRequest request) {
        logger.info("Earning points for user: {}, amount: {}", 
            request.getUserId(), request.getPoints());
        
        UserPointsDto userPoints = userPointsService.earnPoints(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(userPoints);
    }

    /**
     * Spend points
     * Kullanıcıdan puan harcama (ödeme sırasında çağrılır)
     */
    @PostMapping("/spend")
    public ResponseEntity<UserPointsDto> spendPoints(@Valid @RequestBody SpendPointsRequest request) {
        logger.info("Spending points for user: {}, amount: {}", 
            request.getUserId(), request.getPoints());
        
        UserPointsDto userPoints = userPointsService.spendPoints(request);
        
        return ResponseEntity.ok(userPoints);
    }

    /**
     * Check if user has enough points
     * Puan yeterlilik kontrolü
     */
    @GetMapping("/{userId}/check/{requiredPoints}")
    public ResponseEntity<Boolean> checkPoints(
            @PathVariable String userId,
            @PathVariable java.math.BigDecimal requiredPoints) {
        
        logger.info("Checking if user {} has {} points", userId, requiredPoints);
        
        boolean hasEnough = userPointsService.hasEnoughPoints(userId, requiredPoints);
        
        return ResponseEntity.ok(hasEnough);
    }

    /**
     * Health check endpoint for user points service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User points service is running");
    }
}
