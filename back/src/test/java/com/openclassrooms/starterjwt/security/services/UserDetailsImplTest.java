package com.openclassrooms.starterjwt.security.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDetailsImpl Tests")
class UserDetailsImplTest {

    private UserDetailsImpl userDetails;
    private final Long id = 1L;
    private final String username = "test@test.com";
    private final String firstName = "John";
    private final String lastName = "Doe";
    private final String password = "password123";
    private final Boolean admin = true;

    @BeforeEach
    void setUp() {
        userDetails = UserDetailsImpl.builder()
                .id(id)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .password(password)
                .admin(admin)
                .build();
    }

    @Nested
    @DisplayName("Constructor and Builder Tests")
    class ConstructorAndBuilderTests {

        @Test
        @DisplayName("Should create UserDetailsImpl with all properties using builder")
        void shouldCreateUserDetailsImplWithAllProperties() {
            assertAll("User Details Properties",
                    () -> assertEquals(id, userDetails.getId()),
                    () -> assertEquals(username, userDetails.getUsername()),
                    () -> assertEquals(firstName, userDetails.getFirstName()),
                    () -> assertEquals(lastName, userDetails.getLastName()),
                    () -> assertEquals(password, userDetails.getPassword()),
                    () -> assertEquals(admin, userDetails.getAdmin())
            );
        }
    }

    @Nested
    @DisplayName("UserDetails Interface Implementation Tests")
    class UserDetailsInterfaceTests {

        @Test
        @DisplayName("Should return correct values from UserDetails methods")
        void shouldReturnEmptyAuthorities() {
            assertTrue(userDetails.getAuthorities().isEmpty());
            assertTrue(userDetails.isAccountNonExpired());
            assertTrue(userDetails.isAccountNonLocked());
            assertTrue(userDetails.isCredentialsNonExpired());
            assertTrue(userDetails.isEnabled());
        }
    }
}