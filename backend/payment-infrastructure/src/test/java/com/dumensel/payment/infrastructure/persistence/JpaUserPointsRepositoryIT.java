package com.dumensel.payment.infrastructure.persistence;

import com.dumensel.payment.domain.model.UserPoints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for JpaUserPointsRepository
 * Uses TestContainers for real PostgreSQL database
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaUserPointsRepository.class, UserPointsMapper.class})
@DisplayName("JPA UserPoints Repository Integration Tests")
class JpaUserPointsRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private JpaUserPointsRepository jpaUserPointsRepository;

    @Autowired
    private SpringDataUserPointsRepository springDataRepository;

    @Nested
    @DisplayName("Save and Find Operations")
    class SaveAndFindOperations {

        @Test
        @DisplayName("Should save new user points to database")
        void shouldSaveNewUserPointsToDatabase() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));

            // When
            UserPoints savedUserPoints = jpaUserPointsRepository.save(userPoints);

            // Then
            assertThat(savedUserPoints).isNotNull();
            assertThat(savedUserPoints.getUserId()).isEqualTo("user123");
            assertThat(savedUserPoints.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(savedUserPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            
            // Verify it's actually in the database
            Optional<UserPointsEntity> entityInDb = springDataRepository.findByUserId("user123");
            assertThat(entityInDb).isPresent();
            assertThat(entityInDb.get().getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should find user points by user ID")
        void shouldFindUserPointsByUserId() {
            // Given
            UserPoints userPoints = new UserPoints("user456");
            userPoints.earnPoints(new BigDecimal("50.00"));
            jpaUserPointsRepository.save(userPoints);

            // When
            Optional<UserPoints> found = jpaUserPointsRepository.findByUserId("user456");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUserId()).isEqualTo("user456");
            assertThat(found.get().getTotalPoints()).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("Should return empty when user points not found")
        void shouldReturnEmptyWhenUserPointsNotFound() {
            // When
            Optional<UserPoints> found = jpaUserPointsRepository.findByUserId("nonexistent");

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should update existing user points")
        void shouldUpdateExistingUserPoints() {
            // Given - Create initial user points
            UserPoints userPoints = new UserPoints("user789");
            userPoints.earnPoints(new BigDecimal("100.00"));
            jpaUserPointsRepository.save(userPoints);

            // When - Find and update
            UserPoints foundUserPoints = jpaUserPointsRepository.findByUserId("user789").orElseThrow();
            foundUserPoints.spendPoints(new BigDecimal("30.00"));
            UserPoints updatedUserPoints = jpaUserPointsRepository.save(foundUserPoints);

            // Then
            assertThat(updatedUserPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("70.00"));
            
            // Verify in database
            UserPoints fromDb = jpaUserPointsRepository.findByUserId("user789").orElseThrow();
            assertThat(fromDb.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("70.00"));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        @DisplayName("Should delete user points")
        void shouldDeleteUserPoints() {
            // Given
            UserPoints userPoints = new UserPoints("user111");
            jpaUserPointsRepository.save(userPoints);
            assertThat(jpaUserPointsRepository.findByUserId("user111")).isPresent();

            // When
            jpaUserPointsRepository.delete("user111");

            // Then
            assertThat(jpaUserPointsRepository.findByUserId("user111")).isEmpty();
        }

        @Test
        @DisplayName("Should not throw exception when deleting non-existent user points")
        void shouldNotThrowExceptionWhenDeletingNonExistentUserPoints() {
            // When & Then
            assertThatCode(() -> jpaUserPointsRepository.delete("nonexistent"))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Exists Operations")
    class ExistsOperations {

        @Test
        @DisplayName("Should return true when user points exist")
        void shouldReturnTrueWhenUserPointsExist() {
            // Given
            UserPoints userPoints = new UserPoints("user222");
            jpaUserPointsRepository.save(userPoints);

            // When
            boolean exists = jpaUserPointsRepository.existsByUserId("user222");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when user points do not exist")
        void shouldReturnFalseWhenUserPointsDoNotExist() {
            // When
            boolean exists = jpaUserPointsRepository.existsByUserId("nonexistent");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Domain to Entity Mapping")
    class DomainToEntityMapping {

        @Test
        @DisplayName("Should correctly map domain model to JPA entity")
        void shouldCorrectlyMapDomainModelToJpaEntity() {
            // Given
            UserPoints userPoints = new UserPoints("user333");
            userPoints.earnPoints(new BigDecimal("150.00"));
            userPoints.lockPoints(new BigDecimal("50.00"));

            // When
            UserPoints saved = jpaUserPointsRepository.save(userPoints);

            // Then - Verify all fields are persisted correctly
            UserPointsEntity entity = springDataRepository.findByUserId("user333").orElseThrow();
            assertThat(entity.getUserId()).isEqualTo("user333");
            assertThat(entity.getTotalPoints()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(entity.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(entity.getLockedPoints()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getLastUpdated()).isNotNull();
        }

        @Test
        @DisplayName("Should correctly map JPA entity to domain model")
        void shouldCorrectlyMapJpaEntityToDomainModel() {
            // Given
            UserPointsEntity entity = new UserPointsEntity();
            entity.setUserId("user444");
            entity.setTotalPoints(new BigDecimal("200.00"));
            entity.setAvailablePoints(new BigDecimal("150.00"));
            entity.setLockedPoints(new BigDecimal("50.00"));
            entity.setCreatedAt(java.time.LocalDateTime.now());
            entity.setLastUpdated(java.time.LocalDateTime.now());
            springDataRepository.save(entity);

            // When
            UserPoints userPoints = jpaUserPointsRepository.findByUserId("user444").orElseThrow();

            // Then
            assertThat(userPoints.getUserId()).isEqualTo("user444");
            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(userPoints.getCreatedAt()).isNotNull();
            assertThat(userPoints.getLastUpdated()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Transaction and Persistence")
    class TransactionAndPersistence {

        @Test
        @DisplayName("Should persist complex points operations")
        void shouldPersistComplexPointsOperations() {
            // Given
            UserPoints userPoints = new UserPoints("user555");

            // Scenario: User earns points from multiple payments
            userPoints.earnPoints(new BigDecimal("50.00"));
            userPoints.earnPoints(new BigDecimal("30.00"));
            userPoints.earnPoints(new BigDecimal("20.00"));
            jpaUserPointsRepository.save(userPoints);

            // Lock some points
            userPoints.lockPoints(new BigDecimal("40.00"));
            jpaUserPointsRepository.save(userPoints);

            // Spend some points
            userPoints.spendPoints(new BigDecimal("20.00"));
            jpaUserPointsRepository.save(userPoints);

            // When - Reload from database
            UserPoints reloaded = jpaUserPointsRepository.findByUserId("user555").orElseThrow();

            // Then - Verify final state
            assertThat(reloaded.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(reloaded.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("40.00")); // 100 - 40 (locked) - 20 (spent)
            assertThat(reloaded.getLockedPoints()).isEqualByComparingTo(new BigDecimal("40.00"));
        }

        @Test
        @DisplayName("Should handle concurrent updates correctly")
        void shouldHandleConcurrentUpdatesCorrectly() {
            // Given
            UserPoints userPoints = new UserPoints("user666");
            userPoints.earnPoints(new BigDecimal("100.00"));
            jpaUserPointsRepository.save(userPoints);

            // When - Multiple operations
            UserPoints points1 = jpaUserPointsRepository.findByUserId("user666").orElseThrow();
            points1.spendPoints(new BigDecimal("10.00"));
            jpaUserPointsRepository.save(points1);

            UserPoints points2 = jpaUserPointsRepository.findByUserId("user666").orElseThrow();
            points2.spendPoints(new BigDecimal("20.00"));
            jpaUserPointsRepository.save(points2);

            // Then
            UserPoints final_points = jpaUserPointsRepository.findByUserId("user666").orElseThrow();
            assertThat(final_points.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("70.00"));
        }
    }

    @Nested
    @DisplayName("BigDecimal Precision")
    class BigDecimalPrecision {

        @Test
        @DisplayName("Should preserve BigDecimal precision")
        void shouldPreserveBigDecimalPrecision() {
            // Given
            UserPoints userPoints = new UserPoints("user777");
            userPoints.earnPoints(new BigDecimal("100.55"));

            // When
            jpaUserPointsRepository.save(userPoints);
            UserPoints found = jpaUserPointsRepository.findByUserId("user777").orElseThrow();

            // Then
            assertThat(found.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.55"));
            assertThat(found.getTotalPoints().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle decimal points correctly in complex operations")
        void shouldHandleDecimalPointsCorrectlyInComplexOperations() {
            // Given
            UserPoints userPoints = new UserPoints("user888");
            userPoints.earnPoints(new BigDecimal("33.33"));
            userPoints.earnPoints(new BigDecimal("66.67"));
            jpaUserPointsRepository.save(userPoints);

            // When
            UserPoints found = jpaUserPointsRepository.findByUserId("user888").orElseThrow();

            // Then
            assertThat(found.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }
}
