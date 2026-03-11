package com.workout.leaderboard.controller;

import com.workout.leaderboard.dto.request.LoginRequest;
import com.workout.leaderboard.dto.request.SignupRequest;
import com.workout.leaderboard.dto.response.AuthResponse;
import com.workout.leaderboard.exception.BadRequestException;
import com.workout.leaderboard.exception.UnauthorizedException;
import com.workout.leaderboard.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login endpoint - accepts email and password.
     * Checks user existence and password from database.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("email and password are required");
        }

        AuthResponse loginResult = authService.login(request.getEmail(), request.getPassword())
                .orElseThrow(() -> new UnauthorizedException("email or password does not match"));
        return ResponseEntity.ok(loginResult);
    }

    /**
     * Signup endpoint - accepts fullName, email and password.
     * Creates user record and returns JWT token.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank() ||
                request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("fullName, email, and password are required");
        }

        AuthResponse response = authService.signup(request.getFullName(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
