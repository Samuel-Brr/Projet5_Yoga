package com.openclassrooms.starterjwt.integration;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void shouldDeleteUserAccount() {
        // Create and save test user
        User user = new User();
        user.setEmail("deletetest@test.com");
        user.setFirstName("Delete");
        user.setLastName("Test");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setAdmin(false);
        User savedUser = userRepository.save(user);
        trackCreatedEntity(User.class, savedUser.getId());

        // Login as the user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(user.getEmail());
        loginRequest.setPassword("password123");

        // Get authentication token
        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                loginRequest,
                JwtResponse.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        String token = loginResponse.getBody().getToken();

        // First verify user exists
        ResponseEntity<UserDto> getUserResponse = restTemplate.exchange(
                baseUrl + "/user/" + savedUser.getId(),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                UserDto.class
        );
        assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        assertNotNull(getUserResponse.getBody());
        assertEquals("deletetest@test.com", getUserResponse.getBody().getEmail());

        // Delete user
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/user/" + savedUser.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(token)),
                Void.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Verify user no longer exists
        assertFalse(userRepository.findByEmail("deletetest@test.com").isPresent(),
                "User should not exist after deletion");

        // Try to access deleted user's data (should fail)
        ResponseEntity<String> getDeletedUserResponse = restTemplate.exchange(
                baseUrl + "/user/" + savedUser.getId(),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, getDeletedUserResponse.getStatusCode());
    }

    @Test
    void shouldNotAllowDeletingOtherUserAccount() {
        // Create two test users
        User user1 = createTestUser("user1@test.com", "User1");
        User user2 = createTestUser("user2@test.com", "User2");

        // Login as user1
        String token = loginUser("user1@test.com", "password123");
        HttpHeaders headers = createAuthHeaders(token);

        // Try to delete user2's account
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                baseUrl + "/user/" + user2.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, deleteResponse.getStatusCode());

        // Verify user2 still exists
        assertTrue(userRepository.findById(user2.getId()).isPresent(),
                "Other user's account should not be deleted");
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() {
        // Login as regular user
        String token = getUserToken();
        HttpHeaders headers = createAuthHeaders(token);

        // Try to get non-existent user
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user/99999",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldRequireAuthenticationForUserOperations() {
        // Create test user
        User testUser = createTestUser("authtest@test.com", "AuthTest");

        // Try to get user details without authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user/" + testUser.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // Helper methods
    private User createTestUser(String email, String firstName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName("Test");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setAdmin(false);
        User savedUser = userRepository.save(user);
        trackCreatedEntity(User.class, savedUser.getId());
        return savedUser;
    }

    private String loginUser(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                loginRequest,
                JwtResponse.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        return loginResponse.getBody().getToken();
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}