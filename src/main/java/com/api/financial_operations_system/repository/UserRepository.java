package com.api.financial_operations_system.repository;

import com.api.financial_operations_system.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndCompany_IdAndDeletedAtIsNull(UUID id, UUID companyId);

    Page<User> findAllByCompany_IdAndDeletedAtIsNull(UUID companyId, Pageable pageable);

    long countByCompany_IdAndRoleAndDeletedAtIsNull(
            UUID companyId,
            com.api.financial_operations_system.domain.user.Role role);
}
