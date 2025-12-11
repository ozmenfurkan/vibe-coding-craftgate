package com.dumensel.payment.infrastructure.persistence;

import com.dumensel.payment.domain.model.UserPoints;
import org.springframework.stereotype.Component;

/**
 * Mapper between UserPoints (Domain) and UserPointsEntity (JPA)
 * Infrastructure Layer
 */
@Component
public class UserPointsMapper {
    
    /**
     * Convert Domain Model to JPA Entity
     */
    public UserPointsEntity toEntity(UserPoints userPoints) {
        UserPointsEntity entity = new UserPointsEntity();
        entity.setUserId(userPoints.getUserId());
        entity.setTotalPoints(userPoints.getTotalPoints());
        entity.setAvailablePoints(userPoints.getAvailablePoints());
        entity.setLockedPoints(userPoints.getLockedPoints());
        entity.setCreatedAt(userPoints.getCreatedAt());
        entity.setLastUpdated(userPoints.getLastUpdated());
        return entity;
    }
    
    /**
     * Convert JPA Entity to Domain Model
     * Uses package-private constructor of UserPoints
     */
    public UserPoints toDomain(UserPointsEntity entity) {
        return new UserPoints(
            entity.getUserId(),
            entity.getTotalPoints(),
            entity.getAvailablePoints(),
            entity.getLockedPoints(),
            entity.getCreatedAt(),
            entity.getLastUpdated()
        );
    }
}
