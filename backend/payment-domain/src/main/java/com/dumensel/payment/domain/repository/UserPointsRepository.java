package com.dumensel.payment.domain.repository;

import com.dumensel.payment.domain.model.UserPoints;
import java.util.Optional;

/**
 * UserPoints Repository Interface
 * Domain Layer - NO implementation here
 * 
 * Infrastructure layer will implement this
 */
public interface UserPointsRepository {
    
    /**
     * Kullanıcı ID'sine göre puan bilgisi getir
     */
    Optional<UserPoints> findByUserId(String userId);
    
    /**
     * Kullanıcı puanlarını kaydet
     */
    UserPoints save(UserPoints userPoints);
    
    /**
     * Kullanıcı puanlarını sil
     */
    void delete(String userId);
    
    /**
     * Kullanıcının puan kaydı var mı kontrolü
     */
    boolean existsByUserId(String userId);
}
