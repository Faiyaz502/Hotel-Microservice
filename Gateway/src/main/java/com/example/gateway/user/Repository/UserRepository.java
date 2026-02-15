package com.example.gateway.user.Repository;


import com.example.gateway.user.Entity.User;
import com.example.gateway.user.Enums.AuthProviderType;
import com.example.gateway.user.Enums.UserRole;
import com.example.gateway.user.Enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<User> findByPrimaryRole(UserRole role, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.primaryRole = :role AND u.status = :status")
    Page<User> findByRoleAndStatus(@Param("role") UserRole role,
                                   @Param("status") UserStatus status,
                                   Pageable pageable);

    Optional<User> findByUsername(String username);

    Boolean existsUserByUsername(String username);
    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProviderType providerType);

    List<User> findByPrimaryRole(UserRole role);
}