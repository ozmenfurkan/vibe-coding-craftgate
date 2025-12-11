package com.dumensel.payment.interfaces.rest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Functional tests for UserPointsController
 * End-to-end API tests with real database (TestContainers)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("UserPoints Controller Functional Tests")
class UserPointsControllerFT {

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

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
    }

    @Nested
    @DisplayName("GET /user-points/{userId}")
    class GetUserPointsEndpoint {

        @Test
        @DisplayName("Should get user points successfully")
        void shouldGetUserPointsSuccessfully() {
            // Given - First create user points by earning
            String userId = "ft-user-001";
            String earnRequest = String.format("""
                {
                  "userId": "%s",
                  "points": 100.00,
                  "reason": "Initial points"
                }
                """, userId);

            given()
                .contentType(ContentType.JSON)
                .body(earnRequest)
            .when()
                .post("/user-points/earn")
            .then()
                .statusCode(201);

            // When & Then
            given()
            .when()
                .get("/user-points/" + userId)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("userId", equalTo(userId))
                .body("totalPoints", equalTo(100.0f))
                .body("availablePoints", equalTo(100.0f))
                .body("lockedPoints", equalTo(0.0f))
                .body("createdAt", notNullValue())
                .body("lastUpdated", notNullValue());
        }

        @Test
        @DisplayName("Should create new user points with zero balance when not exists")
        void shouldCreateNewUserPointsWithZeroBalanceWhenNotExists() {
            // Given
            String userId = "ft-user-new-001";

            // When & Then
            given()
            .when()
                .get("/user-points/" + userId)
            .then()
                .statusCode(200)
                .body("userId", equalTo(userId))
                .body("totalPoints", equalTo(0.0f))
                .body("availablePoints", equalTo(0.0f))
                .body("lockedPoints", equalTo(0.0f));
        }

        @Test
        @DisplayName("Should handle URL encoding for user ID")
        void shouldHandleUrlEncodingForUserId() {
            // Given
            String userId = "user-with-special@chars";

            // When & Then
            given()
            .when()
                .get("/user-points/{userId}", userId)
            .then()
                .statusCode(200)
                .body("userId", equalTo(userId));
        }
    }

    @Nested
    @DisplayName("POST /user-points/earn")
    class EarnPointsEndpoint {

        @Test
        @DisplayName("Should earn points successfully")
        void shouldEarnPointsSuccessfully() {
            // Given
            String userId = "ft-user-002";
            String requestBody = String.format("""
                {
                  "userId": "%s",
                  "points": 50.00,
                  "reason": "Payment completed"
                }
                """, userId);

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post("/user-points/earn")
            .then()
                .statusCode(201)
                .body("userId", equalTo(userId))
                .body("totalPoints", equalTo(50.0f))
                .body("availablePoints", equalTo(50.0f))
                .body("lockedPoints", equalTo(0.0f));
        }

        @Test
        @DisplayName("Should accumulate earned points")
        void shouldAccumulateEarnedPoints() {
            // Given
            String userId = "ft-user-003";

            // First earn
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 30.00,
                      "reason": "First payment"
                    }
                    """, userId))
            .when()
                .post("/user-points/earn")
            .then()
                .statusCode(201);

            // Second earn
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 20.00,
                      "reason": "Second payment"
                    }
                    """, userId))
            .when()
                .post("/user-points/earn")
            .then()
                .statusCode(201)
                .body("totalPoints", equalTo(50.0f))
                .body("availablePoints", equalTo(50.0f));
        }

        @Test
        @DisplayName("Should return 400 for negative points")
        void shouldReturn400ForNegativePoints() {
            // Given
            String requestBody = """
                {
                  "userId": "ft-user-004",
                  "points": -10.00,
                  "reason": "Invalid"
                }
                """;

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post("/user-points/earn")
            .then()
                .statusCode(400)
                .body("title", equalTo("Validation Error"))
                .body("errors.points", notNullValue());
        }

        @Test
        @DisplayName("Should return 400 for missing user ID")
        void shouldReturn400ForMissingUserId() {
            // Given
            String requestBody = """
                {
                  "points": 50.00,
                  "reason": "Missing user ID"
                }
                """;

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post("/user-points/earn")
            .then()
                .statusCode(400)
                .body("title", equalTo("Validation Error"));
        }

        @Test
        @DisplayName("Should return 400 for zero points")
        void shouldReturn400ForZeroPoints() {
            // Given
            String requestBody = """
                {
                  "userId": "ft-user-005",
                  "points": 0.00,
                  "reason": "Zero points"
                }
                """;

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post("/user-points/earn")
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("POST /user-points/spend")
    class SpendPointsEndpoint {

        @Test
        @DisplayName("Should spend points successfully")
        void shouldSpendPointsSuccessfully() {
            // Given - First earn some points
            String userId = "ft-user-006";
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 100.00,
                      "reason": "Initial points"
                    }
                    """, userId))
            .when()
                .post("/user-points/earn");

            // When & Then - Spend some points
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 30.00,
                      "reason": "Used in payment"
                    }
                    """, userId))
            .when()
                .post("/user-points/spend")
            .then()
                .statusCode(200)
                .body("totalPoints", equalTo(100.0f)) // Total never decreases
                .body("availablePoints", equalTo(70.0f))
                .body("lockedPoints", equalTo(0.0f));
        }

        @Test
        @DisplayName("Should return 400 for insufficient points")
        void shouldReturn400ForInsufficientPoints() {
            // Given
            String userId = "ft-user-007";
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 50.00,
                      "reason": "Initial points"
                    }
                    """, userId))
            .when()
                .post("/user-points/earn");

            // When & Then - Try to spend more than available
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 100.00,
                      "reason": "Too much"
                    }
                    """, userId))
            .when()
                .post("/user-points/spend")
            .then()
                .statusCode(409) // Conflict - Business rule violation
                .body("title", equalTo("Business Rule Violation"))
                .body("detail", containsString("Insufficient available points"));
        }

        @Test
        @DisplayName("Should return 404 when user points not found")
        void shouldReturn404WhenUserPointsNotFound() {
            // Given
            String userId = "ft-user-nonexistent";
            String requestBody = String.format("""
                {
                  "userId": "%s",
                  "points": 30.00,
                  "reason": "Trying to spend"
                }
                """, userId);

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post("/user-points/spend")
            .then()
                .statusCode(404)
                .body("title", equalTo("User Points Not Found"));
        }
    }

    @Nested
    @DisplayName("GET /user-points/{userId}/check/{requiredPoints}")
    class CheckPointsEndpoint {

        @Test
        @DisplayName("Should return true when user has enough points")
        void shouldReturnTrueWhenUserHasEnoughPoints() {
            // Given
            String userId = "ft-user-008";
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 100.00,
                      "reason": "Initial points"
                    }
                    """, userId))
            .when()
                .post("/user-points/earn");

            // When & Then
            given()
            .when()
                .get("/user-points/{userId}/check/{requiredPoints}", userId, 50.00)
            .then()
                .statusCode(200)
                .body(equalTo("true"));
        }

        @Test
        @DisplayName("Should return false when user doesn't have enough points")
        void shouldReturnFalseWhenUserDoesNotHaveEnoughPoints() {
            // Given
            String userId = "ft-user-009";
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 50.00,
                      "reason": "Initial points"
                    }
                    """, userId))
            .when()
                .post("/user-points/earn");

            // When & Then
            given()
            .when()
                .get("/user-points/{userId}/check/{requiredPoints}", userId, 100.00)
            .then()
                .statusCode(200)
                .body(equalTo("false"));
        }

        @Test
        @DisplayName("Should return false when user not found")
        void shouldReturnFalseWhenUserNotFound() {
            // When & Then
            given()
            .when()
                .get("/user-points/{userId}/check/{requiredPoints}", "nonexistent", 50.00)
            .then()
                .statusCode(200)
                .body(equalTo("false"));
        }
    }

    @Nested
    @DisplayName("GET /user-points/health")
    class HealthEndpoint {

        @Test
        @DisplayName("Should return 200 for health check")
        void shouldReturn200ForHealthCheck() {
            // When & Then
            given()
            .when()
                .get("/user-points/health")
            .then()
                .statusCode(200)
                .body(containsString("running"));
        }
    }

    @Nested
    @DisplayName("Complete User Journey")
    class CompleteUserJourney {

        @Test
        @DisplayName("Should handle complete user points lifecycle")
        void shouldHandleCompleteUserPointsLifecycle() {
            String userId = "ft-user-journey-001";

            // Step 1: Get user points (should create new with zero)
            given()
            .when()
                .get("/user-points/" + userId)
            .then()
                .statusCode(200)
                .body("totalPoints", equalTo(0.0f))
                .body("availablePoints", equalTo(0.0f));

            // Step 2: Earn points from first payment
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 50.00,
                      "reason": "First payment"
                    }
                    """, userId))
            .when()
                .post("/user-points/earn")
            .then()
                .statusCode(201)
                .body("totalPoints", equalTo(50.0f));

            // Step 3: Earn more points from second payment
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 30.00,
                      "reason": "Second payment"
                    }
                    """, userId))
            .when()
                .post("/user-points/earn")
            .then()
                .statusCode(201)
                .body("totalPoints", equalTo(80.0f));

            // Step 4: Check points availability
            given()
            .when()
                .get("/user-points/{userId}/check/{requiredPoints}", userId, 50.00)
            .then()
                .statusCode(200)
                .body(equalTo("true"));

            // Step 5: Spend some points
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                      "userId": "%s",
                      "points": 20.00,
                      "reason": "Used in payment"
                    }
                    """, userId))
            .when()
                .post("/user-points/spend")
            .then()
                .statusCode(200)
                .body("availablePoints", equalTo(60.0f));

            // Step 6: Verify final balance
            given()
            .when()
                .get("/user-points/" + userId)
            .then()
                .statusCode(200)
                .body("totalPoints", equalTo(80.0f))
                .body("availablePoints", equalTo(60.0f))
                .body("lockedPoints", equalTo(0.0f));
        }
    }
}
