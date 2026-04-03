package com.api.financial_operations_system.repository;

import com.api.financial_operations_system.domain.order.FinancialOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FinancialOrderRepository extends JpaRepository<FinancialOrder, UUID> {
}
