package com.example.demo.entity;

/**
 * @author Victoria Ha
 * @version 0.1.0
 * Description: tests. wow i still have this autoformat from software design.. how cute!
 * @since 10/21/2026
 */

import com.example.demo.service.UserService;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/** Tests Google account linking without a database or real Google tokens. */
class GoogleAccountLinkingTest {

    private UserRepository repository;
    private UserService service;

    /** Supplies mock dependencies without starting Spring or contacting Google. */
    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        JwtDecoder jwtDecoder = mock(JwtDecoder.class);

        service = new UserService(repository, jwtDecoder);
    }

    /** First login creates a normie with a name and four-digit suffix. */
    @Test
    void createsNormieForNewGoogleAccount() {
        when(repository.findByGoogleSub("google-sub-123"))
                .thenReturn(Optional.empty());
        when(repository.findByUsername(anyString()))
                .thenReturn(Optional.empty());
        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.connectGoogleAccount(
                "google-sub-123",
                "Victoria"
        );

        assertEquals("google-sub-123", result.getGoogleSub());
        assertTrue(result.getUsername().matches("Victoria[0-9]{4}"));
        assertFalse(result.isAdmin());
        verify(repository).save(result);
    }

    /** Repeat login preserves the same account, username, and admin role. */
    @Test
    void reusesExistingAdminAccount() {
        User existing = new User();
        existing.setId(1L);
        existing.setGoogleSub("google-sub-123");
        existing.setUsername("Victoria0001");
        existing.setAdmin(true);

        when(repository.findByGoogleSub("google-sub-123"))
                .thenReturn(Optional.of(existing));

        User result = service.connectGoogleAccount(
                "google-sub-123",
                "DifferentName"
        );

        assertSame(existing, result);
        assertEquals("Victoria0001", result.getUsername());
        assertTrue(result.isAdmin());
        verify(repository, never()).save(any(User.class));
        verify(repository, never()).findByUsername(anyString());
    }

    /** Missing Google names receive the fallback prefix. */
    @Test
    void usesFallbackWhenFirstNameIsMissing() {
        when(repository.findByGoogleSub("google-sub-456"))
                .thenReturn(Optional.empty());
        when(repository.findByUsername(anyString()))
                .thenReturn(Optional.empty());
        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.connectGoogleAccount(
                "google-sub-456",
                null
        );

        assertTrue(result.getUsername().matches("User[0-9]{4}"));
        assertFalse(result.isAdmin());
    }

    /** Stops after 25 attempts when every candidate is already taken. */
    @Test
    void stopsAfterTwentyFiveTakenUsernames() {
        when(repository.findByGoogleSub("google-sub-789"))
                .thenReturn(Optional.empty());
        when(repository.findByUsername(anyString()))
                .thenReturn(Optional.of(new User()));

        var error = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.connectGoogleAccount(
                        "google-sub-789",
                        "Victoria"
                )
        );

        assertEquals(503, error.getStatusCode().value());
        verify(repository, times(25)).findByUsername(anyString());
        verify(repository, never()).save(any(User.class));
    }
}
