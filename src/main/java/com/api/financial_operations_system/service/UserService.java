package com.api.financial_operations_system.service;

import com.api.financial_operations_system.domain.company.Company;
import com.api.financial_operations_system.domain.user.Role;
import com.api.financial_operations_system.domain.user.User;
import com.api.financial_operations_system.dto.auth.RegisterRequest;
import com.api.financial_operations_system.dto.auth.RegisterResponse;
import com.api.financial_operations_system.dto.user.CreateUserRequest;
import com.api.financial_operations_system.dto.user.UpdateUserRequest;
import com.api.financial_operations_system.dto.user.UserResponse;
import com.api.financial_operations_system.repository.CompanyRepository;
import com.api.financial_operations_system.repository.UserRepository;
import com.api.financial_operations_system.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CurrentUserService currentUserService;

    @Value("${app.jwt.expiration-minutes:1440}")
    private long expiresInMinutes;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.email())) {
            throw new IllegalArgumentException("Email já registado");
        }
        Company company = companyRepository.save(Company.builder()
                .id(UUID.randomUUID())
                .name(request.companyName())
                .build());
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .company(company)
                .build();
        userRepository.save(user);
        String token = jwtTokenService.createToken(user);
        return new RegisterResponse(user.getId(), company.getId(), user.getEmail(), token, "Bearer", expiresInMinutes);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        UUID companyId = currentUserService.requireCompanyId();
        return userRepository.findAllByCompany_IdAndDeletedAtIsNull(companyId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        UUID companyId = currentUserService.requireCompanyId();
        User user = userRepository.findByIdAndCompany_IdAndDeletedAtIsNull(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(user);
    }

    public UserResponse create(CreateUserRequest request) {
        UUID companyId = currentUserService.requireCompanyId();
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.email())) {
            throw new IllegalArgumentException("Email já registado");
        }
        Company company = companyRepository.getReferenceById(companyId);
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .company(company)
                .build();
        return toResponse(userRepository.save(user));
    }

    public UserResponse update(UUID id, UpdateUserRequest request) {
        UUID companyId = currentUserService.requireCompanyId();
        User user = userRepository.findByIdAndCompany_IdAndDeletedAtIsNull(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.email() != null && !request.email().isBlank()) {
            String e = request.email().trim().toLowerCase();
            if (!e.equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(e)) {
                throw new IllegalArgumentException("Email já registado");
            }
            user.setEmail(e);
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        return toResponse(userRepository.save(user));
    }

    public void delete(UUID id) {
        UUID companyId = currentUserService.requireCompanyId();
        UUID currentUserId = currentUserService.requireUserId();
        if (id.equals(currentUserId)) {
            throw new IllegalStateException("Não pode desativar a própria conta");
        }
        User user = userRepository.findByIdAndCompany_IdAndDeletedAtIsNull(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() == Role.ADMIN
                && userRepository.countByCompany_IdAndRoleAndDeletedAtIsNull(companyId, Role.ADMIN) <= 1) {
            throw new IllegalStateException("Não pode desativar o último ADMIN ativo da empresa");
        }
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCompany().getId(),
                user.getCompany().getName(),
                user.getDeletedAt() == null);
    }
}

