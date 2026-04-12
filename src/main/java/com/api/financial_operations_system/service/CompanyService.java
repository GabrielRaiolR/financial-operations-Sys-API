package com.api.financial_operations_system.service;

import com.api.financial_operations_system.domain.company.Company;
import com.api.financial_operations_system.dto.company.CompanyResponse;
import com.api.financial_operations_system.dto.company.CreateCompanyRequest;
import com.api.financial_operations_system.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CurrentUserService currentUserService;

    public CompanyResponse createCompany(CreateCompanyRequest request) {
        Company company = Company.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .build();
        Company saved = companyRepository.save(company);

        return new CompanyResponse(saved.getId(), saved.getName());
    }

    @Transactional(readOnly = true)
    public CompanyResponse getById(UUID id) {
        UUID tenantId = currentUserService.requireCompanyId();
        if (!tenantId.equals(id)) {
            throw new IllegalArgumentException("Company not found");
        }
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        return new CompanyResponse(company.getId(), company.getName());
    }
}
