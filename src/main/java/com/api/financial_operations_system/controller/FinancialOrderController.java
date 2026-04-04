package com.api.financial_operations_system.controller;

import com.api.financial_operations_system.domain.order.OrderStatus;
import com.api.financial_operations_system.dto.order.CreateFinancialOrderRequest;
import com.api.financial_operations_system.dto.order.FinancialOrderResponse;
import com.api.financial_operations_system.dto.order.RejectFinancialOrderRequest;
import com.api.financial_operations_system.service.CurrentUserService;
import com.api.financial_operations_system.service.FinancialOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/financial-orders")
@RequiredArgsConstructor
public class FinancialOrderController {

    private final FinancialOrderService financialOrderService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<FinancialOrderResponse> create(@Valid @RequestBody CreateFinancialOrderRequest request) {
        FinancialOrderResponse response = financialOrderService.createFinancialOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<FinancialOrderResponse> approve(@PathVariable UUID id) {
        UUID companyId = currentUserService.requireCompanyId();
        return ResponseEntity.ok(financialOrderService.approve(companyId, id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<FinancialOrderResponse> reject(@PathVariable UUID id, @RequestBody(required = false) RejectFinancialOrderRequest body) {
        UUID companyId = currentUserService.requireCompanyId();
        String reason = body != null ? body.reason() : null;
        return ResponseEntity.ok(financialOrderService.reject(companyId, id, reason));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<FinancialOrderResponse> list(@RequestParam(required = false)OrderStatus status, Pageable pageable) {
        UUID companyId = currentUserService.requireCompanyId();
        return financialOrderService.list(companyId, status, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialOrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(financialOrderService.getById(id));
    }
}
