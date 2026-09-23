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
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /*
     * Public registration is currently disabled.
     *
     * HyrvoAI is multi-tenant, so a normal local account
     * should eventually be created through a company flow.
     */
    public User register(
            RegisterRequest request
    ) {

        throw new IllegalStateException(
                "Public registration is currently disabled. "
                        + "Please contact your company administrator."
        );
    }

    public AuthResponse login(
            LoginRequest request
    ) {

        if (request == null
                || request.getEmail() == null
                || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new RuntimeException(
                    "Email and password are required"
            );
        }

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid email or password"
                                )
                        );

        /*
         * OAuth-only accounts do not have a local password.
         */
        if (user.getPassword() == null
                || user.getPassword().isBlank()) {

            throw new RuntimeException(
                    "This account uses social login. "
                            + "Please continue with Google or LinkedIn."
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        /*
         * Local accounts currently require a company.
         */
        if (user.getCompany() == null) {

            throw new RuntimeException(
                    "Your account is not associated with a company."
            );
        }

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getName(),
                        user.getRole()
                );

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}