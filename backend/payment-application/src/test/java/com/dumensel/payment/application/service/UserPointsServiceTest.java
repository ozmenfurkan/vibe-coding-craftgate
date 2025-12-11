package com.dumensel.payment.application.service;

import com.dumensel.payment.application.dto.*;
import com.dumensel.payment.domain.model.UserPoints;
import com.dumensel.payment.domain.repository.UserPointsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserPointsService
 * Tests application layer orchestration with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserPointsService Application Tests")
class UserPointsServiceTest {

    @Mock
    private UserPointsRepository userPointsRepository;

    @InjectMocks
    private UserPointsService userPointsService;

    @Nested
    @DisplayName("Get User Points Use Case")
    class GetUserPointsUseCase {

        @Test
        @DisplayName("Should return existing user points")
        void shouldReturnExistingUserPoints() {
            // Given
            String userId = "user123";
            UserPoints userPoints = new UserPoints(userId);
            userPoints.earnPoints(new BigDecimal("100.00"));

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(userPoints));

            // When
            UserPointsDto result = userPointsService.getUserPoints(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(result.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            
            verify(userPointsRepository, times(1)).findByUserId(userId);
            verify(userPointsRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create new user points when not exists")
        void shouldCreateNewUserPointsWhenNotExists() {
            // Given
            String userId = "newuser123";
            UserPoints newUserPoints = new UserPoints(userId);

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.empty());
            when(userPointsRepository.save(any(UserPoints.class)))
                .thenReturn(newUserPoints);

            // When
            UserPointsDto result = userPointsService.getUserPoints(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getTotalPoints()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getAvailablePoints()).isEqualByComparingTo(BigDecimal.ZERO);
            
            verify(userPointsRepository, times(1)).findByUserId(userId);
            verify(userPointsRepository, times(1)).save(any(UserPoints.class));
        }

        @Test
        @DisplayName("Should map domain model to DTO correctly")
        void shouldMapDomainModelToDtoCorrectly() {
            // Given
            String userId = "user123";
            UserPoints userPoints = new UserPoints(userId);
            userPoints.earnPoints(new BigDecimal("150.00"));
            userPoints.lockPoints(new BigDecimal("50.00"));

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(userPoints));

            // When
            UserPointsDto result = userPointsService.getUserPoints(userId);

            // Then
            assertThat(result.getTotalPoints()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(result.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(result.getLockedPoints()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getLastUpdated()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Earn Points Use Case")
    class EarnPointsUseCase {

        @Test
        @DisplayName("Should earn points for existing user")
        void shouldEarnPointsForExistingUser() {
            // Given
            String userId = "user123";
            UserPoints existingUserPoints = new UserPoints(userId);
            existingUserPoints.earnPoints(new BigDecimal("50.00"));

            EarnPointsRequest request = new EarnPointsRequest();
            request.setUserId(userId);
            request.setPoints(new BigDecimal("30.00"));
            request.setReason("Payment completed");

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingUserPoints));
            when(userPointsRepository.save(any(UserPoints.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UserPointsDto result = userPointsService.earnPoints(request);

            // Then
            assertThat(result.getTotalPoints()).isEqualByComparingTo(new BigDecimal("80.00"));
            assertThat(result.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("80.00"));
            
            verify(userPointsRepository, times(1)).findByUserId(userId);
            verify(userPointsRepository, times(1)).save(any(UserPoints.class));
        }

        @Test
        @DisplayName("Should create new user points and earn when not exists")
        void shouldCreateNewUserPointsAndEarnWhenNotExists() {
            // Given
            String userId = "newuser123";
            EarnPointsRequest request = new EarnPointsRequest();
            request.setUserId(userId);
            request.setPoints(new BigDecimal("50.00"));

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.empty());
            when(userPointsRepository.save(any(UserPoints.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UserPointsDto result = userPointsService.earnPoints(request);

            // Then
            assertThat(result.getTotalPoints()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(result.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("50.00"));
            
            verify(userPointsRepository, times(1)).findByUserId(userId);
            verify(userPointsRepository, times(1)).save(any(UserPoints.class));
        }

        @Test
        @DisplayName("Should handle domain validation for invalid points")
        void shouldHandleDomainValidationForInvalidPoints() {
            // Given
            String userId = "user123";
            UserPoints userPoints = new UserPoints(userId);

            EarnPointsRequest request = new EarnPointsRequest();
            request.setUserId(userId);
            request.setPoints(new BigDecimal("-10.00")); // Invalid negative points

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(userPoints));

            // When & Then
            assertThatThrownBy(() -> userPointsService.earnPoints(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Points must be positive");
            
            verify(userPointsRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Spend Points Use Case")
    class SpendPointsUseCase {

        @Test
        @DisplayName("Should spend points successfully")
        void shouldSpendPointsSuccessfully() {
            // Given
            String userId = "user123";
            UserPoints userPoints = new UserPoints(userId);
            userPoints.earnPoints(new BigDecimal("100.00"));

            SpendPointsRequest request = new SpendPointsRequest();
            request.setUserId(userId);
            request.setPoints(new BigDecimal("30.00"));
            request.setReason("Used in payment");

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(userPoints));
            when(userPointsRepository.save(any(UserPoints.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UserPointsDto result = userPointsService.spendPoints(request);

            // Then
            assertThat(result.getTotalPoints()).isEqualByComparingTo(new BigDecimal("100.00")); // Total never decreases
            assertThat(result.getAvailablePoints()).isEqualByComparingTo(new BigDecimal("70.00"));
            
            verify(userPointsRepository, times(1)).findByUserId(userId);
            verify(userPointsRepository, times(1)).save(any(UserPoints.class));
        }

        @Test
        @DisplayName("Should throw exception when user points not found")
        void shouldThrowExceptionWhenUserPointsNotFound() {
            // Given
            String userId = "nonexistent";
            SpendPointsRequest request = new SpendPointsRequest();
            request.setUserId(userId);
            request.setPoints(new BigDecimal("30.00"));

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userPointsService.spendPoints(request))
                .isInstanceOf(UserPointsNotFoundException.class)
                .hasMessageContaining("User points not found for userId: " + userId);
            
            verify(userPointsRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for insufficient points")
        void shouldThrowExceptionForInsufficientPoints() {
            // Given
            String userId = "user123";
            UserPoints userPoints = new UserPoints(userId);
            userPoints.earnPoints(new BigDecimal("50.00"));

            SpendPointsRequest request = new SpendPointsRequest();
            request.setUserId(userId);
            request.setPoints(new BigDecimal("100.00")); // More than available

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(userPoints));

            // When & Then
            assertThatThrownBy(() -> userPointsService.spendPoints(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient available points");
            
            verify(userPointsRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Check Points Availability Use Case")
    class CheckPointsAvailabilityUseCase {

        @Test
        @DisplayName("Should return true when user has enough points")
        void shouldReturnTrueWhenUserHasEnoughPoints() {
            // Given
            String userId = "user123";
            UserPoints userPoints = new UserPoints(userId);
            userPoints.earnPoints(new BigDecimal("100.00"));

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(userPoints));

            // When
            boolean result = userPointsService.hasEnoughPoints(userId, new BigDecimal("50.00"));

            // Then
            assertThat(result).isTrue();
            verify(userPointsRepository, times(1)).findByUserId(userId);
        }

        @Test
        @DisplayName("Should return false when user doesn't have enough points")
        void shouldReturnFalseWhenUserDoesNotHaveEnoughPoints() {
            // Given
            String userId = "user123";
            UserPoints userPoints = new UserPoints(userId);
            userPoints.earnPoints(new BigDecimal("50.00"));

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(userPoints));

            // When
            boolean result = userPointsService.hasEnoughPoints(userId, new BigDecimal("100.00"));

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when user points not found")
        void shouldReturnFalseWhenUserPointsNotFound() {
            // Given
            String userId = "nonexistent";

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

            // When
            boolean result = userPointsService.hasEnoughPoints(userId, new BigDecimal("50.00"));

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when checking for zero available points")
        void shouldReturnFalseWhenCheckingForZeroAvailablePoints() {
            // Given
            String userId = "user123";
            UserPoints userPoints = new UserPoints(userId);
            // No points earned

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(userPoints));

            // When
            boolean result = userPointsService.hasEnoughPoints(userId, new BigDecimal("1.00"));

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("DTO Mapping")
    class DtoMapping {

        @Test
        @DisplayName("Should map all fields from domain to DTO")
        void shouldMapAllFieldsFromDomainToDto() {
            // Given
            String userId = "user123";
            UserPoints userPoints = new UserPoints(userId);
            userPoints.earnPoints(new BigDecimal("100.00"));
            userPoints.lockPoints(new BigDecimal("30.00"));

            when(userPointsRepository.findByUserId(userId))
                .thenReturn(Optional.of(userPoints));

            // When
            UserPointsDto dto = userPointsService.getUserPoints(userId);

            // Then
            assertThat(dto.getUserId()).isEqualTo(userPoints.getUserId());
            assertThat(dto.getTotalPoints()).isEqualByComparingTo(userPoints.getTotalPoints());
            assertThat(dto.getAvailablePoints()).isEqualByComparingTo(userPoints.getAvailablePoints());
            assertThat(dto.getLockedPoints()).isEqualByComparingTo(userPoints.getLockedPoints());
            assertThat(dto.getCreatedAt()).isEqualTo(userPoints.getCreatedAt());
            assertThat(dto.getLastUpdated()).isEqualTo(userPoints.getLastUpdated());
        }
    }
}
