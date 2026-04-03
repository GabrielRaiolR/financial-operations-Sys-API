package com.api.financial_operations_system.domain.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    private UUID id;
    private String name;

    /** NOT NULL: use DEFAULT no SQL/Flyway se já houver linhas em {@code companies}. */
    @ColumnDefault("0")
    @Column(name = "auto_approval_limit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal autoApprovalLimit = BigDecimal.ZERO;
}
