package com.openclassrooms.starterjwt.security.jwt;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthTokenFilter Tests")
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    private UserDetails userDetails;
    private static final String TOKEN = "valid.jwt.token";
    private static final String USERNAME = "test@test.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username(USERNAME)
                .firstName("John")
                .lastName("Doe")
                .password("password")
                .build();
    }

    @Nested
    @DisplayName("Token Parsing Tests")
    class TokenParsingTests {

        @Test
        @DisplayName("Should parse valid authorization header")
        void shouldParseValidAuthorizationHeader() throws Exception {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
            when(jwtUtils.validateJwtToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getUserNameFromJwtToken(TOKEN)).thenReturn(USERNAME);
            when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateJwtToken(TOKEN);
            verify(jwtUtils).getUserNameFromJwtToken(TOKEN);
            verify(userDetailsService).loadUserByUsername(USERNAME);
            verify(filterChain).doFilter(request, response);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should handle token validation exception")
        void shouldHandleTokenValidationException() throws Exception {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
            when(jwtUtils.validateJwtToken(TOKEN)).thenThrow(new RuntimeException("Token validation failed"));

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateJwtToken(TOKEN);
            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @BeforeEach
        void setUp() {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
            when(jwtUtils.validateJwtToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getUserNameFromJwtToken(TOKEN)).thenReturn(USERNAME);
        }

        @Test
        @DisplayName("Should set authentication in context for valid token")
        void shouldSetAuthenticationInContextForValidToken() throws Exception {
            // Arrange
            when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        }
    }

    @Nested
    @DisplayName("Filter Chain Tests")
    class FilterChainTests {

        @Test
        @DisplayName("Should continue filter chain after successful authentication")
        void shouldContinueFilterChainAfterSuccessfulAuthentication() throws Exception {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
            when(jwtUtils.validateJwtToken(TOKEN)).thenReturn(true);
            when(jwtUtils.getUserNameFromJwtToken(TOKEN)).thenReturn(USERNAME);
            when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }
}