package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtils Tests")
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // Set up test configuration using ReflectionTestUtils
        String jwtSecret = "bezKoderSecretKey";
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        // 1 hour
        int jwtExpirationMs = 3600000;
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);

        // Create UserDetails
        userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("test@test.com")
                .firstName("John")
                .lastName("Doe")
                .password("password")
                .build();
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid JWT token")
        void shouldGenerateValidJwtToken() {
            // Arrange
            when(authentication.getPrincipal()).thenReturn(userDetails);

            // Act
            String token = jwtUtils.generateJwtToken(authentication);

            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(jwtUtils.validateJwtToken(token));
            assertEquals("test@test.com", jwtUtils.getUserNameFromJwtToken(token));
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate correct token")
        void shouldValidateCorrectToken() {
            // Arrange
            when(authentication.getPrincipal()).thenReturn(userDetails);
            String token = jwtUtils.generateJwtToken(authentication);

            // Act
            boolean isValid = jwtUtils.validateJwtToken(token);

            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should reject malformed token")
        void shouldRejectMalformedToken() {
            // Arrange
            String malformedToken = "malformed.jwt.token";

            // Act
            boolean isValid = jwtUtils.validateJwtToken(malformedToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            // Act
            boolean isValid = jwtUtils.validateJwtToken(null);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should reject empty token")
        void shouldRejectEmptyToken() {
            // Act
            boolean isValid = jwtUtils.validateJwtToken("");

            // Assert
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Username Extraction Tests")
    class UsernameExtractionTests {

        @Test
        @DisplayName("Should extract correct username from token")
        void shouldExtractCorrectUsername() {
            // Arrange
            when(authentication.getPrincipal()).thenReturn(userDetails);
            String token = jwtUtils.generateJwtToken(authentication);

            // Act
            String username = jwtUtils.getUserNameFromJwtToken(token);

            // Assert
            assertEquals("test@test.com", username);
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            // Arrange
            String invalidToken = "invalid.token.structure";

            // Act & Assert
            assertThrows(Exception.class, () -> jwtUtils.getUserNameFromJwtToken(invalidToken));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle SignatureException")
        void shouldHandleSignatureException() {
            // Arrange
            String invalidSignatureToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIn0." +
                    "invalid_signature";

            // Act
            boolean isValid = jwtUtils.validateJwtToken(invalidSignatureToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should handle MalformedJwtException")
        void shouldHandleMalformedJwtException() {
            // Arrange
            String malformedToken = "malformed.token";

            // Act
            boolean isValid = jwtUtils.validateJwtToken(malformedToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should handle ExpiredJwtException")
        void shouldHandleExpiredJwtException() {
            // Arrange - Set a very short expiration time
            ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1); // 1ms
            when(authentication.getPrincipal()).thenReturn(userDetails);
            String token = jwtUtils.generateJwtToken(authentication);

            // Wait for token to expire
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Act
            boolean isValid = jwtUtils.validateJwtToken(token);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should handle UnsupportedJwtException")
        void shouldHandleUnsupportedJwtException() {
            // Arrange - Create an unsupported JWT token format
            String unsupportedToken = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0." +
                    "eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIn0.";

            // Act
            boolean isValid = jwtUtils.validateJwtToken(unsupportedToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException")
        void shouldHandleIllegalArgumentException() {
            // Act
            boolean isValid = jwtUtils.validateJwtToken("");

            // Assert
            assertFalse(isValid);
        }
    }
}