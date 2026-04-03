package com.api.financial_operations_system.service;

import com.api.financial_operations_system.domain.company.Company;
import com.api.financial_operations_system.domain.order.FinancialOrder;
import com.api.financial_operations_system.domain.order.OrderStatus;
import com.api.financial_operations_system.dto.order.CreateFinancialOrderRequest;
import com.api.financial_operations_system.dto.order.FinancialOrderResponse;
import com.api.financial_operations_system.repository.CompanyRepository;
import com.api.financial_operations_system.repository.FinancialOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialOrderService {

    private final FinancialOrderRepository financialOrderRepository;
    private final CompanyRepository companyRepository;
    private final CurrentUserService currentUserService;

    public FinancialOrderResponse createFinancialOrder(CreateFinancialOrderRequest request) {
        UUID companyId = currentUserService.requireCompanyId();
        LocalDateTime now = LocalDateTime.now();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        FinancialOrder order = FinancialOrder.builder()
                .id(UUID.randomUUID())
                .company(company)
                .amount(request.amount())
                .orderType(request.type())
                .orderStatus(OrderStatus.PENDING)
                .description(request.description())
                .createdAt(now)
                .updatedAt(now)
                .build();

        FinancialOrder saved = financialOrderRepository.save(order);

        return new FinancialOrderResponse(
                saved.getId(),
                company.getId(),
                saved.getAmount(),
                saved.getOrderType(),
                saved.getOrderStatus(),
                saved.getDescription(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public FinancialOrderResponse getById(UUID id) {
        UUID companyId = currentUserService.requireCompanyId();
        FinancialOrder order =  financialOrderRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        Company company = order.getCompany();
        return new FinancialOrderResponse(
                order.getId(),
                company.getId(),
                order.getAmount(),
                order.getOrderType(),
                order.getOrderStatus(),
                order.getDescription(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
