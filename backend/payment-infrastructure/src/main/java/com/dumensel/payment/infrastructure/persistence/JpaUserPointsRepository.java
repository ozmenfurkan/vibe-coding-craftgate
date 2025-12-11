package com.dumensel.payment.infrastructure.persistence;

import com.dumensel.payment.domain.model.UserPoints;
import com.dumensel.payment.domain.repository.UserPointsRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Implementation of UserPointsRepository
 * Infrastructure Layer - Implements domain repository interface
 */
@Repository
public class JpaUserPointsRepository implements UserPointsRepository {
    
    private final SpringDataUserPointsRepository springDataRepository;
    private final UserPointsMapper mapper;

    public JpaUserPointsRepository(SpringDataUserPointsRepository springDataRepository, 
                                    UserPointsMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<UserPoints> findByUserId(String userId) {
        return springDataRepository.findByUserId(userId)
            .map(mapper::toDomain);
    }

    @Override
    public UserPoints save(UserPoints userPoints) {
        UserPointsEntity entity = mapper.toEntity(userPoints);
        UserPointsEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void delete(String userId) {
        springDataRepository.deleteById(userId);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return springDataRepository.existsByUserId(userId);
    }
}
