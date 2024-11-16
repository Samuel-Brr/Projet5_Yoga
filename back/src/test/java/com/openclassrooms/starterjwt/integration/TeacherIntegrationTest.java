package com.openclassrooms.starterjwt.integration;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

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

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}