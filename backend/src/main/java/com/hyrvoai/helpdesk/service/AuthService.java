package com.hyrvoai.helpdesk.service;

import com.hyrvoai.helpdesk.dto.auth.AuthResponse;
import com.hyrvoai.helpdesk.dto.auth.LoginRequest;
import com.hyrvoai.helpdesk.dto.auth.RegisterRequest;
import com.hyrvoai.helpdesk.entity.User;
import com.hyrvoai.helpdesk.repository.UserRepository;
import com.hyrvoai.helpdesk.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(
            RegisterRequest request) {

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new RuntimeException(
                    "Email already registered");
        }

        String department =
                request.getDepartment();

        if (department == null
                || department.isBlank()) {

            department = "GENERAL";
        }

        User user =
                new User(
                        request.getEmail(),
                        passwordEncoder.encode(
                                request.getPassword()),
                        request.getName(),
                        "EMPLOYEE",
                        department
                );

        return userRepository.save(user);
    }

    public AuthResponse login(
            LoginRequest request) {

        User user =
                userRepository
                        .findByEmail(
                                request.getEmail())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid email or password");
        }

        String token =
                jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}