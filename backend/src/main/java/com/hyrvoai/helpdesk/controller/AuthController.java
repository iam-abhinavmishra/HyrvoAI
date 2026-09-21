package com.hyrvoai.helpdesk.controller;

import com.hyrvoai.helpdesk.dto.auth.AuthResponse;
import com.hyrvoai.helpdesk.dto.auth.LoginRequest;
import com.hyrvoai.helpdesk.dto.auth.RegisterRequest;
import com.hyrvoai.helpdesk.entity.User;
import com.hyrvoai.helpdesk.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        user.setPassword(null);

        return user;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}