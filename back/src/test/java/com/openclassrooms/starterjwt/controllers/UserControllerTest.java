package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.mapper.UserMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private User mockUser;
    private UserDto mockUserDto;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPassword("hashedPassword");
        mockUser.setAdmin(false);
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());

        mockUserDto = new UserDto();
        mockUserDto.setId(1L);
        mockUserDto.setEmail("test@test.com");
        mockUserDto.setFirstName("John");
        mockUserDto.setLastName("Doe");
        mockUserDto.setAdmin(false);
        mockUserDto.setCreatedAt(mockUser.getCreatedAt());
        mockUserDto.setUpdatedAt(mockUser.getUpdatedAt());
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return user when found")
        void shouldReturnUserWhenFound() {
            when(userService.findById(1L)).thenReturn(mockUser);
            when(userMapper.toDto(mockUser)).thenReturn(mockUserDto);

            ResponseEntity<?> response = userController.findById("1");

            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals(mockUserDto, response.getBody());
            verify(userService).findById(1L);
            verify(userMapper).toDto(mockUser);
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete user successfully when authorized")
        void shouldDeleteUserSuccessfullyWhenAuthorized() {
            // Arrange
            when(userService.findById(1L)).thenReturn(mockUser);

            // Setup security context
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            UserDetails userDetails = mock(UserDetails.class);

            when(userDetails.getUsername()).thenReturn("test@test.com");
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            try {
                // Act
                ResponseEntity<?> response = userController.save("1");

                // Assert
                assertTrue(response.getStatusCode().is2xxSuccessful());
                verify(userService).findById(1L);
                verify(userService).delete(1L);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }
}