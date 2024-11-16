package com.openclassrooms.starterjwt.integration;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldRegisterAndLoginUser() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("test@test.com");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");
        signupRequest.setPassword("password123");

        // Act - Register
        ResponseEntity<?> registerResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                signupRequest,
                String.class
        );

        // Assert registration
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());

        // Find created user and track for cleanup
        User createdUser = userRepository.findByEmail(signupRequest.getEmail()).orElse(null);
        assertNotNull(createdUser);
        trackCreatedEntity(User.class, createdUser.getId());

        // Act - Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(signupRequest.getEmail());
        loginRequest.setPassword(signupRequest.getPassword());

        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                loginRequest,
                JwtResponse.class
        );

        // Assert login
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertNotNull(loginResponse.getBody().getToken());
    }
}