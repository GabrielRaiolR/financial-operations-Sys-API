package com.api.financial_operations_system.domain.user;

import com.api.financial_operations_system.domain.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private UUID id;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="company_id", nullable = false)
    private Company company;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}
