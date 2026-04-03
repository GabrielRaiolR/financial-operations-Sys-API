package com.api.financial_operations_system.dto.company;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyRequest(@NotBlank
                                   String name) {
}
