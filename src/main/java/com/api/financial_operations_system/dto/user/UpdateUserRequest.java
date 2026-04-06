package com.api.financial_operations_system.dto.user;

import com.api.financial_operations_system.domain.user.Role;
import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
        @Email String email,
        Role role
) {
}
