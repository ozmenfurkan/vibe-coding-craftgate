package com.dumensel.payment.application.service;

import com.dumensel.payment.application.dto.*;
import com.dumensel.payment.domain.model.UserPoints;
import com.dumensel.payment.domain.repository.UserPointsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserPoints Application Service
 * Orchestrates user points use cases and handles transactions
 */
@Service
public class UserPointsService {
    
    private final UserPointsRepository userPointsRepository;

    public UserPointsService(UserPointsRepository userPointsRepository) {
        this.userPointsRepository = userPointsRepository;
    }

    /**
     * Kullanıcı puan sorgulama use case
     * Kullanıcının mevcut puan durumunu getirir
     */
    @Transactional(readOnly = true)
    public UserPointsDto getUserPoints(String userId) {
        UserPoints userPoints = userPointsRepository.findByUserId(userId)
            .orElseGet(() -> {
                // Kullanıcının puan kaydı yoksa yeni bir tane oluştur
                UserPoints newUserPoints = new UserPoints(userId);
                return userPointsRepository.save(newUserPoints);
            });
        
        return mapToDto(userPoints);
    }

    /**
     * Puan kazanma use case
     * Başarılı ödeme sonrası puan eklenir
     */
    @Transactional
    public UserPointsDto earnPoints(EarnPointsRequest request) {
        UserPoints userPoints = userPointsRepository.findByUserId(request.getUserId())
            .orElseGet(() -> new UserPoints(request.getUserId()));
        
        // Domain logic: Puan kazanma
        userPoints.earnPoints(request.getPoints());
        
        // Save and return
        userPoints = userPointsRepository.save(userPoints);
        return mapToDto(userPoints);
    }

    /**
     * Puan harcama use case
     * Ödeme sırasında puan kullanımı
     */
    @Transactional
    public UserPointsDto spendPoints(SpendPointsRequest request) {
        UserPoints userPoints = userPointsRepository.findByUserId(request.getUserId())
            .orElseThrow(() -> new UserPointsNotFoundException(
                "User points not found for userId: " + request.getUserId()));
        
        // Domain logic: Puan harcama
        userPoints.spendPoints(request.getPoints());
        
        // Save and return
        userPoints = userPointsRepository.save(userPoints);
        return mapToDto(userPoints);
    }

    /**
     * Yeterli puan kontrolü
     * Ödeme öncesi puan yeterlilik kontrolü
     */
    @Transactional(readOnly = true)
    public boolean hasEnoughPoints(String userId, java.math.BigDecimal requiredPoints) {
        return userPointsRepository.findByUserId(userId)
            .map(userPoints -> userPoints.hasEnoughPoints(requiredPoints))
            .orElse(false);
    }

    // Domain <-> DTO mapping
    private UserPointsDto mapToDto(UserPoints userPoints) {
        UserPointsDto dto = new UserPointsDto();
        dto.setUserId(userPoints.getUserId());
        dto.setTotalPoints(userPoints.getTotalPoints());
        dto.setAvailablePoints(userPoints.getAvailablePoints());
        dto.setLockedPoints(userPoints.getLockedPoints());
        dto.setLastUpdated(userPoints.getLastUpdated());
        dto.setCreatedAt(userPoints.getCreatedAt());
        return dto;
    }
}
