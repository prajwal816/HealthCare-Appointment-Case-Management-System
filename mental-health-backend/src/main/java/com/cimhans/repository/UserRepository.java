package com.cimhans.repository;

import com.cimhans.domain.entity.User;
import com.cimhans.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.role = :role")
    Page<User> findAllByRoleAndNotDeleted(@Param("role") Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<User> searchUsers(@Param("q") String query, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.role = :role AND u.isActive = true")
    long countActiveByRole(@Param("role") Role role);
}
