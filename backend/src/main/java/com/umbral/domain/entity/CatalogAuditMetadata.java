package com.umbral.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners({CatalogAuditResetListener.class, AuditingEntityListener.class})
public abstract class CatalogAuditMetadata {
    @CreatedBy
    @Column(name = "created_by_user_id", updatable = false)
    private Long createdByUserId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getCreatedByUserId() { return createdByUserId; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getUpdatedByUserId() { return updatedByUserId; }
    public Instant getUpdatedAt() { return updatedAt; }

    void clearUpdatedByUserId() {
        this.updatedByUserId = null;
    }
}
