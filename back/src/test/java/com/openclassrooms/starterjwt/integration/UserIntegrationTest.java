package com.openclassrooms.starterjwt.integration;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldManageUserLifecycle() {
        // Create test user
        User user = new User();
        user.setEmail("testuser@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setAdmin(false);
        User savedUser = userRepository.save(user);
        trackCreatedEntity(User.class, savedUser.getId());

        // Login as the user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(user.getEmail());
        loginRequest.setPassword("password123");

        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                loginRequest,
                JwtResponse.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String token = loginResponse.getBody().getToken();

        // Get user details
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<UserDto> getUserResponse = restTemplate.exchange(
                baseUrl + "/user/" + savedUser.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserDto.class
        );

        assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        assertEquals(user.getEmail(), getUserResponse.getBody().getEmail());
    }
}