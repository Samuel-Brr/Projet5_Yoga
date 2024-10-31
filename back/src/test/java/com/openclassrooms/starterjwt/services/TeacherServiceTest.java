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

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Tests")
public class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher mockTeacher1;
    private Teacher mockTeacher2;

    @BeforeEach
    void setUp() {
        // Initialize test data
        mockTeacher1 = new Teacher();
        mockTeacher1.setId(1L);
        mockTeacher1.setFirstName("John");
        mockTeacher1.setLastName("Doe");

        mockTeacher2 = new Teacher();
        mockTeacher2.setId(2L);
        mockTeacher2.setFirstName("Jane");
        mockTeacher2.setLastName("Smith");
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return list of all teachers when teachers exist")
        void shouldReturnAllTeachersWhenTeachersExist() {
            // Arrange
            List<Teacher> expectedTeachers = Arrays.asList(mockTeacher1, mockTeacher2);
            when(teacherRepository.findAll()).thenReturn(expectedTeachers);

            // Act
            List<Teacher> result = teacherService.findAll();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(expectedTeachers, result);
            verify(teacherRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no teachers exist")
        void shouldReturnEmptyListWhenNoTeachersExist() {
            // Arrange
            when(teacherRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<Teacher> result = teacherService.findAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(teacherRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return teacher when found")
        void shouldReturnTeacherWhenFound() {
            // Arrange
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(mockTeacher1));

            // Act
            Teacher result = teacherService.findById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(mockTeacher1.getId(), result.getId());
            assertEquals(mockTeacher1.getFirstName(), result.getFirstName());
            assertEquals(mockTeacher1.getLastName(), result.getLastName());
            verify(teacherRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return null when teacher not found")
        void shouldReturnNullWhenTeacherNotFound() {
            // Arrange
            when(teacherRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Teacher result = teacherService.findById(999L);

            // Assert
            assertNull(result);
            verify(teacherRepository, times(1)).findById(999L);
        }
    }
}