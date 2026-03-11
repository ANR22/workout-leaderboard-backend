package com.workout.leaderboard.controller;

import com.workout.leaderboard.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "email and password are required"
            ));
        }

        Optional<Map<String, Object>> loginResult = authService.login(email, password);
        if (loginResult.isPresent()) {
            return ResponseEntity.ok(loginResult.get());
        }

        return ResponseEntity.status(401).body(Map.of(
                "status", "ERROR",
                "message", "email or password does not match"
        ));
    }

    /**
     * Signup endpoint - accepts fullName, email and password.
     * Creates user record and returns JWT token.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {
        String fullName = request.get("fullName");
        String email = request.get("email");
        String password = request.get("password");

        // Validation
        if (fullName == null || fullName.isEmpty() ||
                email == null || email.isEmpty() ||
                password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "fullName, email, and password are required"
            ));
        }

        Map<String, Object> signupResult = authService.signup(fullName, email, password);
        if ("ERROR".equals(signupResult.get("status"))) {
            return ResponseEntity.badRequest().body(signupResult);
        }

        return ResponseEntity.ok(signupResult);
    }
}
