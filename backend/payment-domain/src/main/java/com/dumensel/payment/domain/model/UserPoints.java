package com.dumensel.payment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UserPoints Aggregate Root
 * Domain Layer - NO Spring annotations allowed
 * 
 * Kullanıcı puan yönetimi için domain model
 */
public class UserPoints {
    private String userId;
    private BigDecimal totalPoints;
    private BigDecimal availablePoints;
    private BigDecimal lockedPoints; // Bekleyen işlemlerde kilitli puanlar
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    // Business constructor (for creating new user points)
    public UserPoints(String userId) {
        this.userId = userId;
        this.totalPoints = BigDecimal.ZERO;
        this.availablePoints = BigDecimal.ZERO;
        this.lockedPoints = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    // Reconstruction constructor (for loading from database)
    // Package-private: Only infrastructure layer can use this
    UserPoints(String userId, BigDecimal totalPoints, BigDecimal availablePoints, 
               BigDecimal lockedPoints, LocalDateTime createdAt, LocalDateTime lastUpdated) {
        this.userId = userId;
        this.totalPoints = totalPoints;
        this.availablePoints = availablePoints;
        this.lockedPoints = lockedPoints;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Business logic: Puan kazanma
     * Başarılı ödeme sonrası puan eklenir
     */
    public void earnPoints(BigDecimal points) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        
        this.totalPoints = this.totalPoints.add(points);
        this.availablePoints = this.availablePoints.add(points);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Business logic: Puan harcama
     * Ödeme sırasında puan kullanımı
     */
    public void spendPoints(BigDecimal points) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        
        if (this.availablePoints.compareTo(points) < 0) {
            throw new IllegalStateException("Insufficient available points. Available: " 
                + availablePoints + ", Requested: " + points);
        }
        
        this.availablePoints = this.availablePoints.subtract(points);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Business logic: Puan kilitleme
     * Bekleyen işlem için puanları geçici olarak kilitle
     */
    public void lockPoints(BigDecimal points) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        
        if (this.availablePoints.compareTo(points) < 0) {
            throw new IllegalStateException("Insufficient available points to lock");
        }
        
        this.availablePoints = this.availablePoints.subtract(points);
        this.lockedPoints = this.lockedPoints.add(points);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Business logic: Puan kilidini aç
     * İşlem iptal edildiğinde kilitli puanları serbest bırak
     */
    public void unlockPoints(BigDecimal points) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        
        if (this.lockedPoints.compareTo(points) < 0) {
            throw new IllegalStateException("Cannot unlock more points than locked");
        }
        
        this.lockedPoints = this.lockedPoints.subtract(points);
        this.availablePoints = this.availablePoints.add(points);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Business logic: Kilitli puanları tüket
     * Bekleyen işlem onaylandığında kilitli puanları tüket
     */
    public void consumeLockedPoints(BigDecimal points) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        
        if (this.lockedPoints.compareTo(points) < 0) {
            throw new IllegalStateException("Cannot consume more points than locked");
        }
        
        this.lockedPoints = this.lockedPoints.subtract(points);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Business logic: Yeterli puan kontrolü
     */
    public boolean hasEnoughPoints(BigDecimal requiredPoints) {
        return this.availablePoints.compareTo(requiredPoints) >= 0;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public BigDecimal getTotalPoints() {
        return totalPoints;
    }

    public BigDecimal getAvailablePoints() {
        return availablePoints;
    }

    public BigDecimal getLockedPoints() {
        return lockedPoints;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
