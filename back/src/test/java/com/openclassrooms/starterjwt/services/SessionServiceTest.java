package com.openclassrooms.starterjwt.services;

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

import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService Tests")
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionService sessionService;

    private Session mockSession;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Initialize test data
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        mockSession = new Session();
        mockSession.setId(1L);
        mockSession.setName("Yoga Session");
        mockSession.setDescription("Beginner friendly yoga session");
        mockSession.setUsers(new ArrayList<>());
    }

    @Nested
    @DisplayName("Create Session Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create session successfully")
        void shouldCreateSessionSuccessfully() {
            // Arrange
            when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);

            // Act
            Session result = sessionService.create(mockSession);

            // Assert
            assertNotNull(result);
            assertEquals(mockSession.getName(), result.getName());
            assertEquals(mockSession.getDescription(), result.getDescription());
            verify(sessionRepository, times(1)).save(any(Session.class));
        }
    }

    @Nested
    @DisplayName("Delete Session Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete session successfully")
        void shouldDeleteSessionSuccessfully() {
            // Arrange
            doNothing().when(sessionRepository).deleteById(1L);

            // Act
            sessionService.delete(1L);

            // Assert
            verify(sessionRepository, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Find All Sessions Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all sessions when sessions exist")
        void shouldReturnAllSessionsWhenSessionsExist() {
            // Arrange
            List<Session> expectedSessions = Arrays.asList(mockSession);
            when(sessionRepository.findAll()).thenReturn(expectedSessions);

            // Act
            List<Session> result = sessionService.findAll();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(expectedSessions, result);
            verify(sessionRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no sessions exist")
        void shouldReturnEmptyListWhenNoSessionsExist() {
            // Arrange
            when(sessionRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<Session> result = sessionService.findAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(sessionRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Get Session By Id Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return session when found")
        void shouldReturnSessionWhenFound() {
            // Arrange
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(mockSession));

            // Act
            Session result = sessionService.getById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(mockSession.getId(), result.getId());
            assertEquals(mockSession.getName(), result.getName());
            verify(sessionRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return null when session not found")
        void shouldReturnNullWhenSessionNotFound() {
            // Arrange
            when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Session result = sessionService.getById(999L);

            // Assert
            assertNull(result);
            verify(sessionRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("Update Session Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update session successfully")
        void shouldUpdateSessionSuccessfully() {
            // Arrange
            Session updatedSession = mockSession;
            updatedSession.setName("Updated Session");
            when(sessionRepository.save(any(Session.class))).thenReturn(updatedSession);

            // Act
            Session result = sessionService.update(1L, updatedSession);

            // Assert
            assertNotNull(result);
            assertEquals("Updated Session", result.getName());
            assertEquals(1L, result.getId());
            verify(sessionRepository, times(1)).save(any(Session.class));
        }
    }

    @Nested
    @DisplayName("Participation Management Tests")
    class ParticipationTests {

        @Test
        @DisplayName("Should add participant successfully")
        void shouldAddParticipantSuccessfully() {
            // Arrange
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(mockSession));
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);

            // Act
            sessionService.participate(1L, 1L);

            // Assert
            verify(sessionRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).findById(1L);
            verify(sessionRepository, times(1)).save(any(Session.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when session not found for participation")
        void shouldThrowNotFoundExceptionWhenSessionNotFoundForParticipation() {
            // Arrange
            when(sessionRepository.findById(999L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

            // Act & Assert
            assertThrows(NotFoundException.class, () -> sessionService.participate(999L, 1L));
            verify(sessionRepository, times(1)).findById(999L);
            verify(userRepository, times(1)).findById(1L);
            verify(sessionRepository, never()).save(any(Session.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when user already participates")
        void shouldThrowBadRequestExceptionWhenUserAlreadyParticipates() {
            // Arrange
            mockSession.getUsers().add(mockUser);
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(mockSession));
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

            // Act & Assert
            assertThrows(BadRequestException.class, () -> sessionService.participate(1L, 1L));
            verify(sessionRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).findById(1L);
            verify(sessionRepository, never()).save(any(Session.class));
        }

        @Test
        @DisplayName("Should remove participant successfully")
        void shouldRemoveParticipantSuccessfully() {
            // Arrange
            mockSession.getUsers().add(mockUser);
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(mockSession));
            when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);

            // Act
            sessionService.noLongerParticipate(1L, 1L);

            // Assert
            verify(sessionRepository, times(1)).findById(1L);
            verify(sessionRepository, times(1)).save(any(Session.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when user does not participate")
        void shouldThrowBadRequestExceptionWhenUserDoesNotParticipate() {
            // Arrange
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(mockSession));

            // Act & Assert
            assertThrows(BadRequestException.class, () -> sessionService.noLongerParticipate(1L, 1L));
            verify(sessionRepository, times(1)).findById(1L);
            verify(sessionRepository, never()).save(any(Session.class));
        }
    }
}