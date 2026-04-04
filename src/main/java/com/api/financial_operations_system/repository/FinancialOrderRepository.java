package com.api.financial_operations_system.repository;

import com.api.financial_operations_system.domain.order.FinancialOrder;
import com.api.financial_operations_system.domain.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinancialOrderRepository extends JpaRepository<FinancialOrder, UUID> {
    Optional<FinancialOrder> findByIdAndCompany_Id(UUID id, UUID companyId);

    Page<FinancialOrder> findAllByCompany_Id(UUID companyId, Pageable pageable);

    Page<FinancialOrder> findAllByCompany_IdAndOrderStatus(
            UUID companyId, OrderStatus orderStatus, Pageable pageable);
}
