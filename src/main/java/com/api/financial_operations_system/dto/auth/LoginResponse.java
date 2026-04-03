package com.api.financial_operations_system.dto.auth;

public record LoginResponse(
        String acessToken,
        String tokenType,
        long expiresInMinutes
) {
}
