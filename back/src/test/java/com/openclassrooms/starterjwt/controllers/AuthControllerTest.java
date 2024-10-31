package com.openclassrooms.starterjwt.controllers;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private UserDetailsImpl userDetails;
    private User user;
    private static final String TEST_JWT = "test.jwt.token";

    @BeforeEach
    void setUp() {
        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");

        // Setup signup request
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@test.com");
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setPassword("password123");

        // Setup UserDetailsImpl
        userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("test@test.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .build();

        // Setup User
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("hashedPassword");
        user.setAdmin(false);
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should authenticate user successfully")
        void shouldAuthenticateUserSuccessfully() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(jwtUtils.generateJwtToken(authentication)).thenReturn(TEST_JWT);
            when(userRepository.findByEmail(userDetails.getUsername()))
                    .thenReturn(Optional.of(user));

            // Act
            ResponseEntity<?> response = authController.authenticateUser(loginRequest);

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertInstanceOf(JwtResponse.class, response.getBody());

            JwtResponse jwtResponse = (JwtResponse) response.getBody();
            assertEquals(TEST_JWT, jwtResponse.getToken());
            assertEquals(userDetails.getId(), jwtResponse.getId());
            assertEquals(userDetails.getUsername(), jwtResponse.getUsername());
            assertEquals(userDetails.getFirstName(), jwtResponse.getFirstName());
            assertEquals(userDetails.getLastName(), jwtResponse.getLastName());
            assertFalse(jwtResponse.getAdmin());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtils).generateJwtToken(authentication);
            verify(userRepository).findByEmail(userDetails.getUsername());
        }

        @Test
        @DisplayName("Should return admin status when user is admin")
        void shouldReturnAdminStatusWhenUserIsAdmin() {
            // Arrange
            user.setAdmin(true);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(jwtUtils.generateJwtToken(authentication)).thenReturn(TEST_JWT);
            when(userRepository.findByEmail(userDetails.getUsername()))
                    .thenReturn(Optional.of(user));

            // Act
            ResponseEntity<?> response = authController.authenticateUser(loginRequest);

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            JwtResponse jwtResponse = (JwtResponse) response.getBody();
            assertTrue(jwtResponse.getAdmin());
        }

        @Test
        @DisplayName("Should handle authentication failure")
        void shouldHandleAuthenticationFailure() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new RuntimeException("Authentication failed"));

            // Act & Assert
            assertThrows(RuntimeException.class, () ->
                    authController.authenticateUser(loginRequest));

            verify(jwtUtils, never()).generateJwtToken(any());
            verify(userRepository, never()).findByEmail(any());
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            // Arrange
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(signupRequest.getPassword()))
                    .thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            ResponseEntity<?> response = authController.registerUser(signupRequest);

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertInstanceOf(MessageResponse.class, response.getBody());
            assertEquals("User registered successfully!",
                    ((MessageResponse) response.getBody()).getMessage());

            verify(userRepository).existsByEmail(signupRequest.getEmail());
            verify(passwordEncoder).encode(signupRequest.getPassword());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should reject registration when email exists")
        void shouldRejectRegistrationWhenEmailExists() {
            // Arrange
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

            // Act
            ResponseEntity<?> response = authController.registerUser(signupRequest);

            // Assert
            assertEquals(400, response.getStatusCodeValue());
            assertNotNull(response.getBody());
            assertInstanceOf(MessageResponse.class, response.getBody());
            assertEquals("Error: Email is already taken!",
                    ((MessageResponse) response.getBody()).getMessage());

            verify(userRepository).existsByEmail(signupRequest.getEmail());
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create non-admin user by default")
        void shouldCreateNonAdminUserByDefault() {
            // Arrange
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(signupRequest.getPassword()))
                    .thenReturn("hashedPassword");

            // Act
            authController.registerUser(signupRequest);

            // Assert
            verify(userRepository).save(argThat(savedUser ->
                    !savedUser.isAdmin()
            ));
        }
    }

    @Test
    @DisplayName("Should handle null user when checking admin status")
    void shouldHandleNullUserWhenCheckingAdminStatus() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(TEST_JWT);
        when(userRepository.findByEmail(userDetails.getUsername()))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertFalse(jwtResponse.getAdmin());
    }
}