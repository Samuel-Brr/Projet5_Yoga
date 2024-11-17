package com.openclassrooms.starterjwt.security;

import com.openclassrooms.starterjwt.security.jwt.AuthEntryPointJwt;
import com.openclassrooms.starterjwt.security.jwt.AuthTokenFilter;
import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSecurityConfig Tests")
class WebSecurityConfigTest {

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private AuthEntryPointJwt unauthorizedHandler;

    @Mock
    private AuthenticationManagerBuilder authManagerBuilder;

    @InjectMocks
    private WebSecurityConfig webSecurityConfig;

    @BeforeEach
    void setUp() {
        // Set dependencies using ReflectionTestUtils
        ReflectionTestUtils.setField(webSecurityConfig, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(webSecurityConfig, "unauthorizedHandler", unauthorizedHandler);
    }

    @Nested
    @DisplayName("Bean Creation Tests")
    class BeanCreationTests {

        @Test
        @DisplayName("Should create authentication token filter")
        void shouldCreateAuthenticationTokenFilter() {
            // Act
            AuthTokenFilter filter = webSecurityConfig.authenticationJwtTokenFilter();

            // Assert
            assertNotNull(filter);
        }

        @Test
        @DisplayName("Should create password encoder")
        void shouldCreatePasswordEncoder() {
            // Act
            PasswordEncoder encoder = webSecurityConfig.passwordEncoder();

            // Assert
            assertNotNull(encoder);
            assertTrue(encoder instanceof BCryptPasswordEncoder);

            // Verify encoder works
            String password = "testPassword";
            String encodedPassword = encoder.encode(password);
            assertTrue(encoder.matches(password, encodedPassword));
            assertFalse(encoder.matches("wrongPassword", encodedPassword));
        }
    }

    @Nested
    @DisplayName("Authentication Configuration Tests")
    class AuthenticationConfigurationTests {

        @Test
        @DisplayName("Should configure AuthenticationManagerBuilder")
        void shouldConfigureAuthenticationManagerBuilder() throws Exception {
            // Arrange
            DaoAuthenticationConfigurer daoAuthenticationConfigurerMock = mock(DaoAuthenticationConfigurer.class);
            when(authManagerBuilder.userDetailsService(any()))
                    .thenReturn(daoAuthenticationConfigurerMock);
            when(daoAuthenticationConfigurerMock.passwordEncoder(any()))
                    .thenReturn(daoAuthenticationConfigurerMock);

            // Act
            webSecurityConfig.configure(authManagerBuilder);

            // Assert
            verify(authManagerBuilder).userDetailsService(userDetailsService);
            verify(daoAuthenticationConfigurerMock).passwordEncoder(any(BCryptPasswordEncoder.class));
        }

        @Test
        @DisplayName("Should handle AuthenticationManagerBuilder configuration error")
        void shouldHandleAuthManagerBuilderConfigError() throws Exception {
            // Arrange
            when(authManagerBuilder.userDetailsService(any()))
                    .thenThrow(new RuntimeException("Configuration error"));

            // Act & Assert
            Exception exception = assertThrows(Exception.class,
                    () -> webSecurityConfig.configure(authManagerBuilder));
            assertEquals("Configuration error", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Password Encoder Tests")
    class PasswordEncoderTests {

        @Test
        @DisplayName("Should correctly encode and verify passwords")
        void shouldCorrectlyEncodeAndVerifyPasswords() {
            // Arrange
            PasswordEncoder encoder = webSecurityConfig.passwordEncoder();
            String rawPassword = "testPassword123";

            // Act
            String encodedPassword = encoder.encode(rawPassword);

            // Assert
            assertNotEquals(rawPassword, encodedPassword);
            assertTrue(encoder.matches(rawPassword, encodedPassword));
            assertFalse(encoder.matches("wrongPassword", encodedPassword));
        }
    }

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("Should create new instance of AuthTokenFilter")
        void shouldCreateNewInstanceOfAuthTokenFilter() {
            // Act
            AuthTokenFilter firstFilter = webSecurityConfig.authenticationJwtTokenFilter();
            AuthTokenFilter secondFilter = webSecurityConfig.authenticationJwtTokenFilter();

            // Assert
            assertNotNull(firstFilter);
            assertNotNull(secondFilter);
            assertNotSame(firstFilter, secondFilter, "Should create new instance each time");
        }
    }
}