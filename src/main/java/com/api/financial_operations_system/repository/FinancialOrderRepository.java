package com.api.financial_operations_system.repository;

import com.api.financial_operations_system.domain.order.FinancialOrder;
import com.api.financial_operations_system.domain.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinancialOrderRepository extends JpaRepository<FinancialOrder, UUID> {
    Optional<FinancialOrder> findByIdCompany_Id(UUID id, UUID company_id);

    Page<FinancialOrder> findAllByCompany_Id(UUID company_id, Pageable pageable);

    Page<FinancialOrder> findAllByCompany_IdAndOrderStatus(
            UUID companyId,
            OrderStatus orderStatus,
            Pageable pageable
    );
}
