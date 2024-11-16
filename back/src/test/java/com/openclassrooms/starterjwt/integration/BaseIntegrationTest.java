// Base Test Class
package com.openclassrooms.starterjwt.integration;

import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TeacherRepository teacherRepository;

    @Autowired
    protected SessionRepository sessionRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected String baseUrl;

    // Store test data references
    protected User adminUser;
    protected User regularUser;
    protected Teacher defaultTeacher;

    // Track created entities for cleanup
    private final ConcurrentHashMap<Class<?>, List<Long>> createdEntities = new ConcurrentHashMap<>();

    @BeforeAll
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        initializeTestData();
    }

    private void initializeTestData() {
        // Create admin user
        adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setAdmin(true);
        adminUser = userRepository.save(adminUser);
        trackCreatedEntity(User.class, adminUser.getId());

        // Create regular user
        regularUser = new User();
        regularUser.setEmail("user@test.com");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setPassword(passwordEncoder.encode("user123"));
        regularUser.setAdmin(false);
        regularUser = userRepository.save(regularUser);
        trackCreatedEntity(User.class, regularUser.getId());

        // Create default teacher
        defaultTeacher = new Teacher();
        defaultTeacher.setFirstName("Default");
        defaultTeacher.setLastName("Teacher");
        defaultTeacher.setCreatedAt(LocalDateTime.now());
        defaultTeacher.setUpdatedAt(LocalDateTime.now());
        defaultTeacher = teacherRepository.save(defaultTeacher);
        trackCreatedEntity(Teacher.class, defaultTeacher.getId());
    }

    @AfterAll
    void cleanup() {
        // Delete entities in reverse order of dependencies
        deleteTrackedEntities(Session.class);
        deleteTrackedEntities(Teacher.class);
        deleteTrackedEntities(User.class);

        // Clear tracking maps
        createdEntities.clear();
    }

    protected void trackCreatedEntity(Class<?> entityClass, Long entityId) {
        createdEntities.computeIfAbsent(entityClass, k -> new ArrayList<>()).add(entityId);
    }

    private void deleteTrackedEntities(Class<?> entityClass) {
        List<Long> ids = createdEntities.getOrDefault(entityClass, new ArrayList<>());
        for (Long id : ids) {
            try {
                if (entityClass == User.class) {
                    userRepository.deleteById(id);
                } else if (entityClass == Teacher.class) {
                    teacherRepository.deleteById(id);
                } else if (entityClass == Session.class) {
                    sessionRepository.deleteById(id);
                }
            } catch (Exception e) {
                System.err.println("Error deleting entity of type " + entityClass + " with id " + id + ": " + e.getMessage());
            }
        }
    }

    // Helper method for getting admin token
    protected String getAdminToken() {
        return login(adminUser.getEmail(), "admin123");
    }

    // Helper method for getting regular user token
    protected String getUserToken() {
        return login(regularUser.getEmail(), "user123");
    }

    // Generic login helper
    protected String login(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                loginRequest,
                JwtResponse.class
        );

        if (response.getBody() == null || response.getBody().getToken() == null) {
            throw new RuntimeException("Failed to login with email: " + email);
        }

        return response.getBody().getToken();
    }
}
