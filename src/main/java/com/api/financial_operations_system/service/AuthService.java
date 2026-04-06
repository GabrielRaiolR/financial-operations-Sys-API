package com.api.financial_operations_system.service;

import com.api.financial_operations_system.domain.user.User;
import com.api.financial_operations_system.dto.auth.LoginRequest;
import com.api.financial_operations_system.dto.auth.LoginResponse;
import com.api.financial_operations_system.repository.UserRepository;
import com.api.financial_operations_system.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @org.springframework.beans.factory.annotation.Value("${app.jwt.expiration-minutes:1440}")
    private long expiresInMinutes;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(request.email())
                .orElseThrow(()
                        -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        String token = jwtTokenService.createToken(user);
        return new LoginResponse(token, "Bearer", expiresInMinutes);
    }

}
