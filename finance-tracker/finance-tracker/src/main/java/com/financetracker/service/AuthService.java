package com.financetracker.service;

import com.financetracker.config.JwtUtil;
import com.financetracker.dto.AuthResponse;
import com.financetracker.dto.LoginRequest;
import com.financetracker.dto.RegisterRequest;
import com.financetracker.exception.BusinessException;
import com.financetracker.model.Role;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Auth Service — handles user registration and login.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     * - Checks if email already exists
     * - Hashes the password
     * - Saves user to DB
     * - Returns a JWT token (user is auto-logged-in after register)
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered. Please login instead.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // hash password!
                .role(Role.USER)
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token, user.getName(), user.getEmail(),
                "Registration successful! Welcome, " + user.getName());
    }

    /**
     * Login an existing user.
     * - Spring Security verifies email + password
     * - If correct, returns a JWT token
     * - If wrong, throws BadCredentialsException (handled by GlobalExceptionHandler)
     */
    public AuthResponse login(LoginRequest request) {
        // This line does the verification — throws BadCredentialsException if wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token, user.getName(), user.getEmail(),
                "Login successful! Welcome back, " + user.getName());
    }
}
