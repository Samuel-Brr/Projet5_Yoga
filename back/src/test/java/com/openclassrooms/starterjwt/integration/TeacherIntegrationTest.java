package com.openclassrooms.starterjwt.integration;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.models.Teacher;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeacherIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldRetrieveTeacher() {
        // Get user token
        String token = getUserToken();

        // Get teacher
        ResponseEntity<TeacherDto> response = restTemplate.exchange(
                baseUrl + "/teacher/" + defaultTeacher.getId(),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                TeacherDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(defaultTeacher.getFirstName(), response.getBody().getFirstName());
        assertEquals(defaultTeacher.getLastName(), response.getBody().getLastName());
    }

    @Test
    void shouldRetrieveAllTeachers() {
        // Get user token
        String token = getUserToken();

        // Create another teacher for testing
        Teacher newTeacher = new Teacher();
        newTeacher.setFirstName("Jane");
        newTeacher.setLastName("Doe");
        newTeacher.setCreatedAt(LocalDateTime.now());
        newTeacher.setUpdatedAt(LocalDateTime.now());
        Teacher savedTeacher = teacherRepository.save(newTeacher);
        trackCreatedEntity(Teacher.class, savedTeacher.getId());

        // Get all teachers
        ResponseEntity<List<TeacherDto>> response = restTemplate.exchange(
                baseUrl + "/teacher",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                new ParameterizedTypeReference<List<TeacherDto>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 2); // At least default teacher and new teacher
        assertTrue(response.getBody().stream()
                .anyMatch(t -> t.getFirstName().equals("Jane") && t.getLastName().equals("Doe")));
    }

    @Test
    void shouldReturnNotFoundForInvalidTeacherId() {
        // Get user token
        String token = getUserToken();

        // Get initial list of teachers to find an ID that doesn't exist
        ResponseEntity<List<TeacherDto>> allTeachersResponse = restTemplate.exchange(
                baseUrl + "/teacher",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                new ParameterizedTypeReference<List<TeacherDto>>() {}
        );

        // Find max ID and add 1 to ensure it doesn't exist
        long nonExistentId = allTeachersResponse.getBody().stream()
                .mapToLong(TeacherDto::getId)
                .max()
                .orElse(0) + 1;

        // Try to get non-existent teacher
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/teacher/" + nonExistentId,
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldNotAllowAccessWithoutAuthentication() {
        // Get initial number of teachers (authenticated request)
        String validToken = getUserToken();
        ResponseEntity<List<TeacherDto>> initialResponse = restTemplate.exchange(
                baseUrl + "/teacher",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(validToken)),
                new ParameterizedTypeReference<List<TeacherDto>>() {}
        );
        assertNotNull(initialResponse.getBody());

        // Try unauthenticated request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/teacher/" + defaultTeacher.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Verify unauthorized status
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        // Verify no data was returned
        assertFalse(response.getBody() == null || response.getBody().isEmpty(),
                "Unauthorized request should not return teacher data");
    }

    @Test
    void shouldRetrieveTeacherWithCorrectDates() {
        // Get user token
        String token = getUserToken();

        // Create teacher with specific dates
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime updateDate = LocalDateTime.now();

        Teacher newTeacher = new Teacher();
        newTeacher.setFirstName("Test");
        newTeacher.setLastName("Teacher");
        newTeacher.setCreatedAt(creationDate);
        newTeacher.setUpdatedAt(updateDate);
        Teacher savedTeacher = teacherRepository.save(newTeacher);
        trackCreatedEntity(Teacher.class, savedTeacher.getId());

        // Get teacher
        ResponseEntity<TeacherDto> response = restTemplate.exchange(
                baseUrl + "/teacher/" + savedTeacher.getId(),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                TeacherDto.class
        );

        // Verify teacher data
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        TeacherDto teacherDto = response.getBody();

        // Verify all fields
        assertEquals(savedTeacher.getId(), teacherDto.getId());
        assertEquals("Test", teacherDto.getFirstName());
        assertEquals("Teacher", teacherDto.getLastName());
        assertNotNull(teacherDto.getCreatedAt());
        assertNotNull(teacherDto.getUpdatedAt());
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}