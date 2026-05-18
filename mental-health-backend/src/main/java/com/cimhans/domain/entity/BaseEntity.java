package com.cimhans.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base entity providing audit fields and soft-delete capability.
 * All domain entities extend this to inherit:
 * - UUID primary key (avoids sequential ID enumeration attacks)
 * - createdAt / updatedAt (auto-populated via JPA auditing)
 * - createdBy / updatedBy (populated by AuditorAwareImpl from security context)
 * - deletedAt (soft delete marker — null means active)
 * - version (optimistic locking to prevent lost updates)
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Soft delete timestamp. Non-null means the record is logically deleted.
     * All queries must filter WHERE deleted_at IS NULL.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Optimistic locking version counter.
     * Prevents concurrent overwrites of the same record.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
