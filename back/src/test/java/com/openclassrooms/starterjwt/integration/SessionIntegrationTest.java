package com.openclassrooms.starterjwt.integration;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SessionIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateAndRetrieveSession() {
        // Get admin token
        String token = getAdminToken();

        // Create session
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Test Session");
        sessionDto.setDescription("Test Description");
        sessionDto.setTeacher_id(defaultTeacher.getId());
        sessionDto.setDate(new Date());

        HttpEntity<SessionDto> request = new HttpEntity<>(sessionDto, createAuthHeaders(token));

        ResponseEntity<SessionDto> createResponse = restTemplate.exchange(
                baseUrl + "/session",
                HttpMethod.POST,
                request,
                SessionDto.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        // Track created session
        Long sessionId = createResponse.getBody().getId();
        trackCreatedEntity(Session.class, sessionId);

        // Verify retrieval
        ResponseEntity<SessionDto> getResponse = restTemplate.exchange(
                baseUrl + "/session/" + sessionId,
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                SessionDto.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(sessionDto.getName(), getResponse.getBody().getName());
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}

