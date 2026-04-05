package com.api.financial_operations_system.domain.order;

import com.api.financial_operations_system.domain.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "financial_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialOrder {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false,precision = 19, scale = 2) //Para usar dinheiro
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus orderStatus;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt; //Rastrear criação
    @Column(nullable = false)
    private LocalDateTime updatedAt; //Auditoria de alterações e sincronizações
}
