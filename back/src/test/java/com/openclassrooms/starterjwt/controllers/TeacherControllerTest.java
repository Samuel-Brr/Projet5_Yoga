package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.mapper.TeacherMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.TeacherService;
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
@DisplayName("TeacherController Tests")
class TeacherControllerTest {

    @Mock
    private TeacherService teacherService;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private TeacherController teacherController;

    private Teacher mockTeacher;
    private TeacherDto mockTeacherDto;
    private List<Teacher> mockTeacherList;
    private List<TeacherDto> mockTeacherDtoList;

    @BeforeEach
    void setUp() {
        // Initialize test data
        mockTeacher = new Teacher();
        mockTeacher.setId(1L);
        mockTeacher.setFirstName("John");
        mockTeacher.setLastName("Doe");
        mockTeacher.setCreatedAt(LocalDateTime.now());
        mockTeacher.setUpdatedAt(LocalDateTime.now());

        mockTeacherDto = new TeacherDto();
        mockTeacherDto.setId(1L);
        mockTeacherDto.setFirstName("John");
        mockTeacherDto.setLastName("Doe");
        mockTeacherDto.setCreatedAt(mockTeacher.getCreatedAt());
        mockTeacherDto.setUpdatedAt(mockTeacher.getUpdatedAt());

        mockTeacherList = Arrays.asList(mockTeacher);
        mockTeacherDtoList = Arrays.asList(mockTeacherDto);
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return teacher when found")
        void shouldReturnTeacherWhenFound() {
            // Arrange
            when(teacherService.findById(1L)).thenReturn(mockTeacher);
            when(teacherMapper.toDto(mockTeacher)).thenReturn(mockTeacherDto);

            // Act
            ResponseEntity<?> response = teacherController.findById("1");

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals(mockTeacherDto, response.getBody());
            verify(teacherService).findById(1L);
            verify(teacherMapper).toDto(mockTeacher);
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all teachers")
        void shouldReturnAllTeachers() {
            // Arrange
            when(teacherService.findAll()).thenReturn(mockTeacherList);
            when(teacherMapper.toDto(mockTeacherList)).thenReturn(mockTeacherDtoList);

            // Act
            ResponseEntity<?> response = teacherController.findAll();

            // Assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals(mockTeacherDtoList, response.getBody());
            verify(teacherService).findAll();
            verify(teacherMapper).toDto(mockTeacherList);
        }
    }
}