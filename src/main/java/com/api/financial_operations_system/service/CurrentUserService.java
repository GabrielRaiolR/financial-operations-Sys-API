package com.api.financial_operations_system.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    public UUID requireCompanyId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("JWT authentication required");
        }
        Jwt jwt = jwtAuth.getToken();
        String raw = firstNonBlank(
                jwt.getClaimAsString("companyId"),
                jwt.getClaimAsString("CompanyId"));

        if (raw == null) {
            Object asObj = jwt.getClaim("companyId");
            if (asObj == null) {
                asObj = jwt.getClaim("CompanyId");
            }
            raw = asObj != null ? asObj.toString() : null;
        }

        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException(
                    "Token sem claim companyId. Use um token novo do POST /auth/login (Authorization: Bearer ...) "
                            + "e confira se o login gera a claim companyId.");
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Valor inválido na claim companyId: " + raw, e);
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
