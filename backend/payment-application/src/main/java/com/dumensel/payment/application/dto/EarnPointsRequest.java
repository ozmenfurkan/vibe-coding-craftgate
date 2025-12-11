package com.dumensel.payment.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for earning points
 * Validation annotations from Jakarta Validation
 */
public class EarnPointsRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Points amount is required")
    @DecimalMin(value = "0.01", message = "Points must be greater than 0")
    private BigDecimal points;
    
    private String reason; // Puan kazanma nedeni (opsiyonel)

    // Default constructor
    public EarnPointsRequest() {
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
