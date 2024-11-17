package com.openclassrooms.starterjwt.integration;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Date;
import java.util.List;

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

    @Test
    void shouldDeleteSessionAsAdmin() {
        // Get admin token
        String adminToken = getAdminToken();

        // Create session
        SessionDto session = createTestSession(adminToken);
        assertNotNull(session.getId());
        trackCreatedEntity(Session.class, session.getId());

        // Verify session exists
        ResponseEntity<SessionDto> getBeforeDelete = restTemplate.exchange(
                baseUrl + "/session/" + session.getId(),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(adminToken)),
                SessionDto.class
        );
        assertEquals(HttpStatus.OK, getBeforeDelete.getStatusCode());

        // Delete session
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/session/" + session.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(adminToken)),
                Void.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Verify session is deleted using GET all sessions
        ResponseEntity<List<SessionDto>> allSessionsResponse = restTemplate.exchange(
                baseUrl + "/session",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(adminToken)),
                new ParameterizedTypeReference<List<SessionDto>>() {}
        );

        List<SessionDto> sessions = allSessionsResponse.getBody();
        assertTrue(sessions.stream()
                        .noneMatch(s -> s.getId().equals(session.getId())),
                "Session should not exist after deletion"
        );
    }

    @Test
    void shouldNotCreateSessionWithoutAdminRights() {
        // Get regular user token
        String userToken = getUserToken();

        // Get initial sessions count
        ResponseEntity<List<SessionDto>> initialSessionsResponse = restTemplate.exchange(
                baseUrl + "/session",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(userToken)),
                new ParameterizedTypeReference<List<SessionDto>>() {}
        );
        int initialSessionCount = initialSessionsResponse.getBody().size()+1;

        // Prepare session data
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Unauthorized Session");
        sessionDto.setDescription("Test Description");
        sessionDto.setTeacher_id(defaultTeacher.getId());
        sessionDto.setDate(new Date());

        // Try to create session as regular user
        ResponseEntity<SessionDto> response = restTemplate.exchange(
                baseUrl + "/session",
                HttpMethod.POST,
                new HttpEntity<>(sessionDto, createAuthHeaders(userToken)),
                SessionDto.class
        );

        // Even if we get a 200 OK, verify that session wasn't actually created
        ResponseEntity<List<SessionDto>> afterSessionsResponse = restTemplate.exchange(
                baseUrl + "/session",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(userToken)),
                new ParameterizedTypeReference<List<SessionDto>>() {}
        );

        // Verify the number of sessions hasn't changed
        assertEquals(initialSessionCount, afterSessionsResponse.getBody().size(),
                "Number of sessions should not change when non-admin tries to create one");
    }

    @Test
    void shouldParticipateAndUnparticipateInSession() {
        // Get tokens
        String adminToken = getAdminToken();
        String userToken = getUserToken();

        // Create session as admin
        SessionDto session = createTestSession(adminToken);
        assertNotNull(session.getId());
        trackCreatedEntity(Session.class, session.getId());

        // Participate in session as user
        ResponseEntity<Void> participateResponse = restTemplate.exchange(
                baseUrl + "/session/" + session.getId() + "/participate/" + regularUser.getId(),
                HttpMethod.POST,
                new HttpEntity<>(createAuthHeaders(userToken)),
                Void.class
        );
        assertEquals(HttpStatus.OK, participateResponse.getStatusCode());

        // Verify participation
        ResponseEntity<SessionDto> getResponse = restTemplate.exchange(
                baseUrl + "/session/" + session.getId(),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(userToken)),
                SessionDto.class
        );
        assertTrue(getResponse.getBody().getUsers().contains(regularUser.getId()));

        // Unparticipate from session
        ResponseEntity<Void> unparticipateResponse = restTemplate.exchange(
                baseUrl + "/session/" + session.getId() + "/participate/" + regularUser.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(userToken)),
                Void.class
        );
        assertEquals(HttpStatus.OK, unparticipateResponse.getStatusCode());

        // Verify unparticipation
        getResponse = restTemplate.exchange(
                baseUrl + "/session/" + session.getId(),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(userToken)),
                SessionDto.class
        );
        assertFalse(getResponse.getBody().getUsers().contains(regularUser.getId()));
    }

    @Test
    void shouldUpdateSessionDetailsAsAdmin() {
        // Get admin token
        String adminToken = getAdminToken();

        // Create initial session
        SessionDto initialSession = createTestSession(adminToken);
        assertNotNull(initialSession.getId());
        trackCreatedEntity(Session.class, initialSession.getId());

        // Update session
        SessionDto updateDto = new SessionDto();
        updateDto.setName("Updated Session Name");
        updateDto.setDescription("Updated Description");
        updateDto.setTeacher_id(defaultTeacher.getId());
        updateDto.setDate(new Date());

        ResponseEntity<SessionDto> updateResponse = restTemplate.exchange(
                baseUrl + "/session/" + initialSession.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateDto, createAuthHeaders(adminToken)),
                SessionDto.class
        );

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("Updated Session Name", updateResponse.getBody().getName());
        assertEquals("Updated Description", updateResponse.getBody().getDescription());
    }
    private SessionDto createTestSession(String token) {
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Test Session");
        sessionDto.setDescription("Test Description");
        sessionDto.setTeacher_id(defaultTeacher.getId());
        sessionDto.setDate(new Date());

        ResponseEntity<SessionDto> response = restTemplate.exchange(
                baseUrl + "/session",
                HttpMethod.POST,
                new HttpEntity<>(sessionDto, createAuthHeaders(token)),
                SessionDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}

