package com.workout.leaderboard.service;

import com.workout.leaderboard.entity.User;
import com.workout.leaderboard.repository.UserRepository;
import com.workout.leaderboard.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Optional<Map<String, Object>> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Optional.empty();
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getFullName());

        return Optional.of(Map.of(
                "status", "SUCCESS",
            "userId", user.getUserId(),
            "fullName", user.getFullName(),
                "token", token,
                "message", "Login successful"
        ));
    }

    public Map<String, Object> signup(String fullName, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            return Map.of(
                    "status", "ERROR",
                    "message", "Email already exists"
            );
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser.getEmail(), savedUser.getFullName());

        return Map.of(
                "status", "SUCCESS",
            "userId", savedUser.getUserId(),
            "fullName", savedUser.getFullName(),
                "token", token,
                "message", "Signup successful. Account created."
        );
    }
}
