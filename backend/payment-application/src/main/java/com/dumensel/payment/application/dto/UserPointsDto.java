package com.dumensel.payment.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UserPoints Data Transfer Object
 * Application Layer - For API responses
 */
public class UserPointsDto {
    private String userId;
    private BigDecimal totalPoints;
    private BigDecimal availablePoints;
    private BigDecimal lockedPoints;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    // Default constructor for Jackson
    public UserPointsDto() {
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(BigDecimal totalPoints) {
        this.totalPoints = totalPoints;
    }

    public BigDecimal getAvailablePoints() {
        return availablePoints;
    }

    public void setAvailablePoints(BigDecimal availablePoints) {
        this.availablePoints = availablePoints;
    }

    public BigDecimal getLockedPoints() {
        return lockedPoints;
    }

    public void setLockedPoints(BigDecimal lockedPoints) {
        this.lockedPoints = lockedPoints;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
