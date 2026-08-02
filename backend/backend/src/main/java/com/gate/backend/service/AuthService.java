package com.gate.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gate.backend.dto.AuthResponse;
import com.gate.backend.dto.LoginRequest;
import com.gate.backend.dto.RegisterRequest;
import com.gate.backend.entity.User;
import com.gate.backend.exception.AppException;
import com.gate.backend.repository.UserRepository;
import com.gate.backend.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ADMIN_EMAIL = "admin@gatepredictor.com";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {

        // Block admin registration
        if (ADMIN_EMAIL.equalsIgnoreCase(request.getEmail())) {
            throw new AppException(
                    "Registration using admin email is not allowed.",
                    HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already registered",
                    HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.STUDENT)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        // Static Admin Login
        if (ADMIN_EMAIL.equalsIgnoreCase(request.getEmail())) {

            if (!ADMIN_PASSWORD.equals(request.getPassword())) {
                throw new AppException(
                        "Invalid email or password",
                        HttpStatus.UNAUTHORIZED);
            }

            String token = jwtUtil.generateToken(ADMIN_EMAIL, "ADMIN");

            return AuthResponse.builder()
                    .token(token)
                    .email(ADMIN_EMAIL)
                    .name("Administrator")
                    .role("ADMIN")
                    .message("Admin login successful")
                    .build();
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(
                        "Invalid email or password",
                        HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new AppException(
                    "Invalid email or password",
                    HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}