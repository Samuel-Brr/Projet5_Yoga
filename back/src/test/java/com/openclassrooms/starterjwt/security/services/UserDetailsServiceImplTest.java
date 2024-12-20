package com.openclassrooms.starterjwt.security.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        // Initialize test data
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPassword("hashedPassword123");
    }

    @Nested
    @DisplayName("loadUserByUsername Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should successfully load user by email")
        void shouldSuccessfullyLoadUserByEmail() {
            // Arrange
            when(userRepository.findByEmail("test@test.com"))
                    .thenReturn(Optional.of(mockUser));

            // Act
            UserDetails result = userDetailsService.loadUserByUsername("test@test.com");

            // Assert
            assertNotNull(result);
            assertInstanceOf(UserDetailsImpl.class, result);
            UserDetailsImpl userDetails = (UserDetailsImpl) result;
            assertEquals(mockUser.getId(), userDetails.getId());
            assertEquals(mockUser.getEmail(), userDetails.getUsername());
            assertEquals(mockUser.getFirstName(), userDetails.getFirstName());
            assertEquals(mockUser.getLastName(), userDetails.getLastName());
            assertEquals(mockUser.getPassword(), userDetails.getPassword());
            verify(userRepository, times(1)).findByEmail("test@test.com");
        }

        @Test
        @DisplayName("Should properly map all user fields")
        void shouldProperlyMapAllUserFields() {
            // Arrange
            mockUser.setAdmin(true); // Add additional field
            when(userRepository.findByEmail("test@test.com"))
                    .thenReturn(Optional.of(mockUser));

            // Act
            UserDetails result = userDetailsService.loadUserByUsername("test@test.com");

            // Assert
            UserDetailsImpl userDetails = (UserDetailsImpl) result;
            assertAll("User field mappings",
                    () -> assertEquals(mockUser.getId(), userDetails.getId()),
                    () -> assertEquals(mockUser.getEmail(), userDetails.getUsername()),
                    () -> assertEquals(mockUser.getFirstName(), userDetails.getFirstName()),
                    () -> assertEquals(mockUser.getLastName(), userDetails.getLastName()),
                    () -> assertEquals(mockUser.getPassword(), userDetails.getPassword()),
                    () -> assertTrue(userDetails.isAccountNonExpired()),
                    () -> assertTrue(userDetails.isAccountNonLocked()),
                    () -> assertTrue(userDetails.isCredentialsNonExpired()),
                    () -> assertTrue(userDetails.isEnabled()),
                    () -> assertNotNull(userDetails.getAuthorities())
            );
        }
    }
}