package com.api.financial_operations_system.service;

import com.api.financial_operations_system.domain.company.Company;
import com.api.financial_operations_system.domain.order.FinancialOrder;
import com.api.financial_operations_system.domain.order.OrderStatus;
import com.api.financial_operations_system.dto.order.CreateFinancialOrderRequest;
import com.api.financial_operations_system.dto.order.FinancialOrderResponse;
import com.api.financial_operations_system.repository.CompanyRepository;
import com.api.financial_operations_system.repository.FinancialOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FinancialOrderResponse getById(UUID id) {
        UUID companyId = currentUserService.requireCompanyId();
        FinancialOrder order = financialOrderRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<FinancialOrderResponse> list(UUID companyId,OrderStatus status,Pageable pageable) {
        Page<FinancialOrder> page = status == null
                ?
                financialOrderRepository.findAllByCompany_Id(companyId, pageable):
                financialOrderRepository.findAllByCompany_IdAndOrderStatus(companyId,status, pageable);
        return page.map(this::toResponse);
    }

    @Transactional
    public FinancialOrderResponse approve(UUID companyId, UUID orderId) {
        FinancialOrder order = financialOrderRepository.findByIdAndCompany_Id(orderId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be approved");
        }
        order.setOrderStatus(OrderStatus.APPROVED);
        order.setUpdatedAt(LocalDateTime.now());
        return toResponse(financialOrderRepository.save(order));
    }

    @Transactional
    public FinancialOrderResponse reject(UUID companyId, UUID orderId, String reason) {
        FinancialOrder order = financialOrderRepository.findByIdAndCompany_Id(orderId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be rejected");
        }
        order.setOrderStatus(OrderStatus.REJECTED);
        order.setUpdatedAt(LocalDateTime.now());
        if (reason != null && !reason.isEmpty()) {
            String desc = order.getDescription();
            String suffix = "[Rejeição]" + reason.trim();
            order.setDescription(desc == null || desc.isBlank() ? suffix : desc + " | " + suffix);
        }
        return toResponse(financialOrderRepository.save(order));
    }

    private FinancialOrderResponse toResponse(FinancialOrder order) {
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
