package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionController Tests")
class SessionControllerTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionController sessionController;

    private Session mockSession;
    private SessionDto mockSessionDto;
    private List<Session> mockSessionList;
    private List<SessionDto> mockSessionDtoList;

    @BeforeEach
    void setUp() {
        // Initialize test data
        mockSession = new Session();
        mockSession.setId(1L);
        mockSession.setName("Yoga Session");
        mockSession.setDescription("Beginner friendly yoga session");
        mockSession.setCreatedAt(LocalDateTime.now());
        mockSession.setUpdatedAt(LocalDateTime.now());

        mockSessionDto = new SessionDto();
        mockSessionDto.setId(1L);
        mockSessionDto.setName("Yoga Session");
        mockSessionDto.setDescription("Beginner friendly yoga session");
        mockSessionDto.setTeacher_id(1L);
        mockSessionDto.setCreatedAt(mockSession.getCreatedAt());
        mockSessionDto.setUpdatedAt(mockSession.getUpdatedAt());

        mockSessionList = Arrays.asList(mockSession);
        mockSessionDtoList = Arrays.asList(mockSessionDto);
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return session when found")
        void shouldReturnSessionWhenFound() {
            // Arrange
            when(sessionService.getById(1L)).thenReturn(mockSession);
            when(sessionMapper.toDto(mockSession)).thenReturn(mockSessionDto);

            // Act
            ResponseEntity<?> response = sessionController.findById("1");

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals(mockSessionDto, response.getBody());
            verify(sessionService).getById(1L);
            verify(sessionMapper).toDto(mockSession);
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all sessions")
        void shouldReturnAllSessions() {
            // Arrange
            when(sessionService.findAll()).thenReturn(mockSessionList);
            when(sessionMapper.toDto(mockSessionList)).thenReturn(mockSessionDtoList);

            // Act
            ResponseEntity<?> response = sessionController.findAll();

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals(mockSessionDtoList, response.getBody());
            verify(sessionService).findAll();
            verify(sessionMapper).toDto(mockSessionList);
        }
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create new session successfully")
        void shouldCreateNewSessionSuccessfully() {
            // Arrange
            when(sessionMapper.toEntity(mockSessionDto)).thenReturn(mockSession);
            when(sessionService.create(mockSession)).thenReturn(mockSession);
            when(sessionMapper.toDto(mockSession)).thenReturn(mockSessionDto);

            // Act
            ResponseEntity<?> response = sessionController.create(mockSessionDto);

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals(mockSessionDto, response.getBody());
            verify(sessionService).create(mockSession);
            verify(sessionMapper).toEntity(mockSessionDto);
            verify(sessionMapper).toDto(mockSession);
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update session successfully")
        void shouldUpdateSessionSuccessfully() {
            // Arrange
            when(sessionMapper.toEntity(mockSessionDto)).thenReturn(mockSession);
            when(sessionService.update(1L, mockSession)).thenReturn(mockSession);
            when(sessionMapper.toDto(mockSession)).thenReturn(mockSessionDto);

            // Act
            ResponseEntity<?> response = sessionController.update("1", mockSessionDto);

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals(mockSessionDto, response.getBody());
            verify(sessionService).update(1L, mockSession);
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete session successfully")
        void shouldDeleteSessionSuccessfully() {
            // Arrange
            when(sessionService.getById(1L)).thenReturn(mockSession);

            // Act
            ResponseEntity<?> response = sessionController.save("1");

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            verify(sessionService).delete(1L);
        }
    }

    @Nested
    @DisplayName("participate Tests")
    class ParticipateTests {

        @Test
        @DisplayName("Should add participation successfully")
        void shouldAddParticipationSuccessfully() {
            // Act
            ResponseEntity<?> response = sessionController.participate("1", "2");

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            verify(sessionService).participate(1L, 2L);
        }
    }

    @Nested
    @DisplayName("noLongerParticipate Tests")
    class NoLongerParticipateTests {

        @Test
        @DisplayName("Should remove participation successfully")
        void shouldRemoveParticipationSuccessfully() {
            // Act
            ResponseEntity<?> response = sessionController.noLongerParticipate("1", "2");

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            verify(sessionService).noLongerParticipate(1L, 2L);
        }
    }
}