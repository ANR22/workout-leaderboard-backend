package com.workout.leaderboard.service;

import com.workout.leaderboard.dto.response.AuthResponse;
import com.workout.leaderboard.entity.User;
import com.workout.leaderboard.exception.ConflictException;
import com.workout.leaderboard.repository.UserRepository;
import com.workout.leaderboard.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsEmptyWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        Optional<AuthResponse> response = authService.login("missing@example.com", "plain-password");

        assertTrue(response.isEmpty());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }

    @Test
    void loginReturnsEmptyWhenPasswordDoesNotMatch() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        Optional<AuthResponse> response = authService.login("user@example.com", "wrong-password");

        assertTrue(response.isEmpty());
        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }

    @Test
    void loginReturnsAuthResponseWhenCredentialsAreValid() {
        User user = new User();
        user.setUserId(7L);
        user.setEmail("user@example.com");
        user.setPassword("hashed-password");
        user.setFullName("Ada Lovelace");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);
        when(jwtTokenProvider.generateToken("user@example.com", "Ada Lovelace")).thenReturn("jwt-token");

        Optional<AuthResponse> response = authService.login("user@example.com", "plain-password");

        assertTrue(response.isPresent());
        assertEquals(7L, response.get().getUserId());
        assertEquals("Ada Lovelace", response.get().getFullName());
        assertEquals("jwt-token", response.get().getToken());
        assertEquals("Login successful", response.get().getMessage());
    }

    @Test
    void signupThrowsConflictWhenEmailAlreadyExists() {
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(
                ConflictException.class,
                () -> authService.signup("New User", "taken@example.com", "plain-password")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void signupEncodesPasswordAndReturnsAuthResponse() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(jwtTokenProvider.generateToken("new@example.com", "Grace Hopper")).thenReturn("signup-token");

        User savedUser = new User();
        savedUser.setUserId(11L);
        savedUser.setEmail("new@example.com");
        savedUser.setFullName("Grace Hopper");
        savedUser.setPassword("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.signup("Grace Hopper", "new@example.com", "plain-password");

        assertEquals(11L, response.getUserId());
        assertEquals("Grace Hopper", response.getFullName());
        assertEquals("signup-token", response.getToken());
        assertEquals("Signup successful. Account created.", response.getMessage());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persistedUser = userCaptor.getValue();
        assertEquals("Grace Hopper", persistedUser.getFullName());
        assertEquals("new@example.com", persistedUser.getEmail());
        assertEquals("encoded-password", persistedUser.getPassword());
        verify(passwordEncoder).encode(eq("plain-password"));
    }
}
