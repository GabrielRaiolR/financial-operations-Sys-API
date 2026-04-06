package com.api.financial_operations_system.dto.auth;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        UUID companyId,
        String email,
        String accessToken,
        String tokenType,
        long expiresInMinutes
) {
}
