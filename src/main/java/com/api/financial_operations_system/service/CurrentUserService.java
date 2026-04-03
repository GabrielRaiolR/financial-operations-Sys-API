package com.api.financial_operations_system.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    public UUID requireCompanyId(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String raw = jwtAuth.getToken().getClaimAsString("companyId");
            return UUID.fromString(raw);
        }
        throw new IllegalStateException("JWT authentication required");
    }
}
