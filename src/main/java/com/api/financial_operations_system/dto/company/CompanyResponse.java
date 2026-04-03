package com.api.financial_operations_system.dto.company;

import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name
) {
}
