package com.dumensel.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for UserPointsEntity
 * Infrastructure Layer
 */
@Repository
public interface SpringDataUserPointsRepository extends JpaRepository<UserPointsEntity, String> {
    
    Optional<UserPointsEntity> findByUserId(String userId);
    
    boolean existsByUserId(String userId);
}
