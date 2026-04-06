package com.api.financial_operations_system.controller;

import com.api.financial_operations_system.dto.auth.LoginRequest;
import com.api.financial_operations_system.dto.auth.LoginResponse;
import com.api.financial_operations_system.dto.auth.RegisterRequest;
import com.api.financial_operations_system.dto.auth.RegisterResponse;
import com.api.financial_operations_system.service.AuthService;
import com.api.financial_operations_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse body = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
