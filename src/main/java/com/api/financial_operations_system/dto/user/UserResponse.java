package com.api.financial_operations_system.dto.user;

import com.api.financial_operations_system.domain.user.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Role role,
        UUID companyId,
        boolean active
) {
}
