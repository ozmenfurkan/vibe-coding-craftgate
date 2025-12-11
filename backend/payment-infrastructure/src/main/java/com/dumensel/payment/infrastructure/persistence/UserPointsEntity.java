package com.dumensel.payment.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UserPoints JPA Entity
 * Infrastructure Layer - Database mapping
 */
@Entity
@Table(name = "user_points")
public class UserPointsEntity {
    
    @Id
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;
    
    @Column(name = "total_points", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPoints;
    
    @Column(name = "available_points", nullable = false, precision = 19, scale = 2)
    private BigDecimal availablePoints;
    
    @Column(name = "locked_points", nullable = false, precision = 19, scale = 2)
    private BigDecimal lockedPoints;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // JPA requires default constructor
    public UserPointsEntity() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
