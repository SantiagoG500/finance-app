package com.finance.account.infraestructure.adapters.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BCryptPasswordEncoderAdapterTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BCryptPasswordEncoderAdapter bcryptPasswordEncoderAdapter;

    @Test
    void shouldEncodeRawPasswordSuccessfully() {
        // Arrange
        String rawPassword = "mySecretPassword";
        when(passwordEncoder.encode(rawPassword)).thenReturn("$2a$10$encodedHash");

        // Act
        String result = bcryptPasswordEncoderAdapter.encode(rawPassword);

        // Assert
        assertEquals("$2a$10$encodedHash", result);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void shouldReturnTrueWhenPasswordMatches() {
        // Arrange
        String rawPassword = "mySecretPassword";
        String encodedHash = "$2a$10$encodedHash";
        when(passwordEncoder.matches(rawPassword, encodedHash)).thenReturn(true);

        // Act
        boolean matches = bcryptPasswordEncoderAdapter.matches(rawPassword, encodedHash);

        // Assert
        assertTrue(matches);
        verify(passwordEncoder).matches(rawPassword, encodedHash);
    }

    @Test
    void shouldReturnFalseWhenPasswordDoesNotMatch() {
        // Arrange
        String rawPassword = "wrongPassword";
        String encodedHash = "$2a$10$encodedHash";
        when(passwordEncoder.matches(rawPassword, encodedHash)).thenReturn(false);

        // Act
        boolean matches = bcryptPasswordEncoderAdapter.matches(rawPassword, encodedHash);

        // Assert
        assertFalse(matches);
        verify(passwordEncoder).matches(rawPassword, encodedHash);
    }
}
