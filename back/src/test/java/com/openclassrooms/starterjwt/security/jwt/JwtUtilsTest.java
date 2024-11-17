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
    }
}