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

        @Test
        @DisplayName("Should create UserDetailsImpl with minimal properties")
        void shouldCreateUserDetailsImplWithMinimalProperties() {
            UserDetailsImpl minimalUser = UserDetailsImpl.builder()
                    .username(username)
                    .password(password)
                    .build();

            assertAll("Minimal User Details Properties",
                    () -> assertNull(minimalUser.getId()),
                    () -> assertEquals(username, minimalUser.getUsername()),
                    () -> assertNull(minimalUser.getFirstName()),
                    () -> assertNull(minimalUser.getLastName()),
                    () -> assertEquals(password, minimalUser.getPassword()),
                    () -> assertNull(minimalUser.getAdmin())
            );
        }
    }

    @Nested
    @DisplayName("UserDetails Interface Implementation Tests")
    class UserDetailsInterfaceTests {

        @Test
        @DisplayName("Should return empty authorities collection")
        void shouldReturnEmptyAuthorities() {
            assertTrue(userDetails.getAuthorities().isEmpty());
        }

        @Test
        @DisplayName("Should return true for account non expired")
        void shouldReturnTrueForAccountNonExpired() {
            assertTrue(userDetails.isAccountNonExpired());
        }

        @Test
        @DisplayName("Should return true for account non locked")
        void shouldReturnTrueForAccountNonLocked() {
            assertTrue(userDetails.isAccountNonLocked());
        }

        @Test
        @DisplayName("Should return true for credentials non expired")
        void shouldReturnTrueForCredentialsNonExpired() {
            assertTrue(userDetails.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("Should return true for enabled")
        void shouldReturnTrueForEnabled() {
            assertTrue(userDetails.isEnabled());
        }
    }

    @Nested
    @DisplayName("Equals Method Tests")
    class EqualsMethodTests {

        @Test
        @DisplayName("Should return true when comparing same object")
        void shouldReturnTrueWhenComparingSameObject() {
            assertEquals(userDetails, userDetails);
        }

        @Test
        @DisplayName("Should return false when comparing with null")
        void shouldReturnFalseWhenComparingWithNull() {
            assertNotEquals(null, userDetails);
        }

        @Test
        @DisplayName("Should return false when comparing with different type")
        void shouldReturnFalseWhenComparingWithDifferentType() {
            assertNotEquals(userDetails, new Object());
        }

        @Test
        @DisplayName("Should return true when comparing objects with same id")
        void shouldReturnTrueWhenComparingObjectsWithSameId() {
            UserDetailsImpl sameIdUser = UserDetailsImpl.builder()
                    .id(id)
                    .username("different@email.com")
                    .password("different")
                    .build();

            assertEquals(userDetails, sameIdUser);
        }

        @Test
        @DisplayName("Should return false when comparing objects with different ids")
        void shouldReturnFalseWhenComparingObjectsWithDifferentIds() {
            UserDetailsImpl differentIdUser = UserDetailsImpl.builder()
                    .id(2L)
                    .username(username)
                    .password(password)
                    .build();

            assertNotEquals(userDetails, differentIdUser);
        }

        @Test
        @DisplayName("Should handle comparison when ids are null")
        void shouldHandleComparisonWhenIdsAreNull() {
            UserDetailsImpl nullIdUser1 = UserDetailsImpl.builder()
                    .username(username)
                    .password(password)
                    .build();

            UserDetailsImpl nullIdUser2 = UserDetailsImpl.builder()
                    .username(username)
                    .password(password)
                    .build();

            assertEquals(nullIdUser1.equals(nullIdUser2), nullIdUser2.equals(nullIdUser1));
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterMethodsTests {

        @Test
        @DisplayName("Should return correct values from all getters")
        void shouldReturnCorrectValuesFromAllGetters() {
            assertAll("Getter Methods",
                    () -> assertEquals(id, userDetails.getId()),
                    () -> assertEquals(username, userDetails.getUsername()),
                    () -> assertEquals(firstName, userDetails.getFirstName()),
                    () -> assertEquals(lastName, userDetails.getLastName()),
                    () -> assertEquals(password, userDetails.getPassword()),
                    () -> assertEquals(admin, userDetails.getAdmin())
            );
        }
    }
}