package com.dumensel.payment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for UserPoints domain model
 * Tests business logic and domain rules
 */
@DisplayName("UserPoints Domain Tests")
class UserPointsTest {

    @Nested
    @DisplayName("UserPoints Creation")
    class UserPointsCreation {

        @Test
        @DisplayName("Should create new user points with zero balance")
        void shouldCreateNewUserPointsWithZeroBalance() {
            // Given
            String userId = "user123";

            // When
            UserPoints userPoints = new UserPoints(userId);

            // Then
            assertThat(userPoints.getUserId()).isEqualTo(userId);
            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(userPoints.getCreatedAt()).isNotNull();
            assertThat(userPoints.getLastUpdated()).isNotNull();
        }

        @Test
        @DisplayName("Should create user points with specific values (reconstruction)")
        void shouldCreateUserPointsWithSpecificValues() {
            // Given
            String userId = "user123";
            BigDecimal totalPoints = new BigDecimal("100.00");
            BigDecimal availablePoints = new BigDecimal("70.00");
            BigDecimal lockedPoints = new BigDecimal("30.00");

            // When
            UserPoints userPoints = new UserPoints(
                userId,
                totalPoints,
                availablePoints,
                lockedPoints,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now()
            );

            // Then
            assertThat(userPoints.getUserId()).isEqualTo(userId);
            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(totalPoints);
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(availablePoints);
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(lockedPoints);
        }
    }

    @Nested
    @DisplayName("Earn Points Business Logic")
    class EarnPointsLogic {

        @Test
        @DisplayName("Should earn points successfully")
        void shouldEarnPointsSuccessfully() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            BigDecimal pointsToEarn = new BigDecimal("50.00");

            // When
            userPoints.earnPoints(pointsToEarn);

            // Then
            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should accumulate earned points")
        void shouldAccumulateEarnedPoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");

            // When
            userPoints.earnPoints(new BigDecimal("50.00"));
            userPoints.earnPoints(new BigDecimal("30.00"));
            userPoints.earnPoints(new BigDecimal("20.00"));

            // Then
            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should throw exception for null points")
        void shouldThrowExceptionForNullPoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");

            // When & Then
            assertThatThrownBy(() -> userPoints.earnPoints(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Points must be positive");
        }

        @Test
        @DisplayName("Should throw exception for zero points")
        void shouldThrowExceptionForZeroPoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");

            // When & Then
            assertThatThrownBy(() -> userPoints.earnPoints(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Points must be positive");
        }

        @Test
        @DisplayName("Should throw exception for negative points")
        void shouldThrowExceptionForNegativePoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");

            // When & Then
            assertThatThrownBy(() -> userPoints.earnPoints(new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Points must be positive");
        }
    }

    @Nested
    @DisplayName("Spend Points Business Logic")
    class SpendPointsLogic {

        @Test
        @DisplayName("Should spend points successfully")
        void shouldSpendPointsSuccessfully() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));

            // When
            userPoints.spendPoints(new BigDecimal("30.00"));

            // Then
            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00")); // Total never decreases
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should throw exception for insufficient available points")
        void shouldThrowExceptionForInsufficientPoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("50.00"));

            // When & Then
            assertThatThrownBy(() -> userPoints.spendPoints(new BigDecimal("100.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient available points")
                .hasMessageContaining("Available: 50")
                .hasMessageContaining("Requested: 100");
        }

        @Test
        @DisplayName("Should spend all available points")
        void shouldSpendAllAvailablePoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));

            // When
            userPoints.spendPoints(new BigDecimal("100.00"));

            // Then
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should throw exception for null points")
        void shouldThrowExceptionForNullPoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");

            // When & Then
            assertThatThrownBy(() -> userPoints.spendPoints(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Points must be positive");
        }
    }

    @Nested
    @DisplayName("Lock Points Business Logic")
    class LockPointsLogic {

        @Test
        @DisplayName("Should lock points successfully")
        void shouldLockPointsSuccessfully() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));

            // When
            userPoints.lockPoints(new BigDecimal("30.00"));

            // Then
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(new BigDecimal("30.00"));
            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should lock multiple times")
        void shouldLockMultipleTimes() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));

            // When
            userPoints.lockPoints(new BigDecimal("20.00"));
            userPoints.lockPoints(new BigDecimal("30.00"));

            // Then
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("Should throw exception for insufficient available points to lock")
        void shouldThrowExceptionForInsufficientPointsToLock() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("50.00"));

            // When & Then
            assertThatThrownBy(() -> userPoints.lockPoints(new BigDecimal("100.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient available points to lock");
        }
    }

    @Nested
    @DisplayName("Unlock Points Business Logic")
    class UnlockPointsLogic {

        @Test
        @DisplayName("Should unlock points successfully")
        void shouldUnlockPointsSuccessfully() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));
            userPoints.lockPoints(new BigDecimal("30.00"));

            // When
            userPoints.unlockPoints(new BigDecimal("30.00"));

            // Then
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should unlock partially")
        void shouldUnlockPartially() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));
            userPoints.lockPoints(new BigDecimal("50.00"));

            // When
            userPoints.unlockPoints(new BigDecimal("20.00"));

            // Then
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(new BigDecimal("30.00"));
        }

        @Test
        @DisplayName("Should throw exception for unlocking more than locked")
        void shouldThrowExceptionForUnlockingMoreThanLocked() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));
            userPoints.lockPoints(new BigDecimal("30.00"));

            // When & Then
            assertThatThrownBy(() -> userPoints.unlockPoints(new BigDecimal("50.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot unlock more points than locked");
        }
    }

    @Nested
    @DisplayName("Consume Locked Points Business Logic")
    class ConsumeLockedPointsLogic {

        @Test
        @DisplayName("Should consume locked points successfully")
        void shouldConsumeLockedPointsSuccessfully() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));
            userPoints.lockPoints(new BigDecimal("30.00"));

            // When
            userPoints.consumeLockedPoints(new BigDecimal("30.00"));

            // Then
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should consume locked points partially")
        void shouldConsumeLockedPointsPartially() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));
            userPoints.lockPoints(new BigDecimal("50.00"));

            // When
            userPoints.consumeLockedPoints(new BigDecimal("20.00"));

            // Then
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(new BigDecimal("30.00"));
        }

        @Test
        @DisplayName("Should throw exception for consuming more than locked")
        void shouldThrowExceptionForConsumingMoreThanLocked() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));
            userPoints.lockPoints(new BigDecimal("30.00"));

            // When & Then
            assertThatThrownBy(() -> userPoints.consumeLockedPoints(new BigDecimal("50.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot consume more points than locked");
        }
    }

    @Nested
    @DisplayName("Points Availability Check")
    class PointsAvailabilityCheck {

        @Test
        @DisplayName("Should return true when has enough points")
        void shouldReturnTrueWhenHasEnoughPoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));

            // When & Then
            assertThat(userPoints.hasEnoughPoints(new BigDecimal("50.00"))).isTrue();
            assertThat(userPoints.hasEnoughPoints(new BigDecimal("100.00"))).isTrue();
        }

        @Test
        @DisplayName("Should return false when doesn't have enough points")
        void shouldReturnFalseWhenDoesNotHaveEnoughPoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("50.00"));

            // When & Then
            assertThat(userPoints.hasEnoughPoints(new BigDecimal("100.00"))).isFalse();
        }

        @Test
        @DisplayName("Should return false for zero available points")
        void shouldReturnFalseForZeroAvailablePoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");

            // When & Then
            assertThat(userPoints.hasEnoughPoints(new BigDecimal("1.00"))).isFalse();
        }

        @Test
        @DisplayName("Should only check available points not locked")
        void shouldOnlyCheckAvailablePointsNotLocked() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));
            userPoints.lockPoints(new BigDecimal("70.00")); // Only 30 available

            // When & Then
            assertThat(userPoints.hasEnoughPoints(new BigDecimal("30.00"))).isTrue();
            assertThat(userPoints.hasEnoughPoints(new BigDecimal("50.00"))).isFalse(); // Has locked but not available
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Should handle complete payment lifecycle with points")
        void shouldHandleCompletePaymentLifecycleWithPoints() {
            // Given
            UserPoints userPoints = new UserPoints("user123");

            // Scenario: User earns points from multiple payments
            userPoints.earnPoints(new BigDecimal("50.00"));  // First payment
            userPoints.earnPoints(new BigDecimal("30.00"));  // Second payment
            userPoints.earnPoints(new BigDecimal("20.00"));  // Third payment

            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("100.00"));

            // Scenario: User locks points for pending payment
            userPoints.lockPoints(new BigDecimal("40.00"));

            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("60.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(new BigDecimal("40.00"));

            // Scenario: Payment confirmed, consume locked points
            userPoints.consumeLockedPoints(new BigDecimal("40.00"));

            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("60.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(BigDecimal.ZERO);

            // Scenario: User spends remaining points
            userPoints.spendPoints(new BigDecimal("60.00"));

            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(userPoints.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00")); // Total never decreases
        }

        @Test
        @DisplayName("Should handle payment cancellation scenario")
        void shouldHandlePaymentCancellationScenario() {
            // Given
            UserPoints userPoints = new UserPoints("user123");
            userPoints.earnPoints(new BigDecimal("100.00"));

            // Lock points for payment
            userPoints.lockPoints(new BigDecimal("50.00"));
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(new BigDecimal("50.00"));

            // Payment cancelled, unlock points
            userPoints.unlockPoints(new BigDecimal("50.00"));

            // Points should be back to available
            assertThat(userPoints.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(userPoints.getLockedPoints()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
