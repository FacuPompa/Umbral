package com.umbral.domain.entity;

import jakarta.persistence.PreUpdate;

/** Runs before Spring auditing so an unknown editor cannot inherit the previous editor's ID. */
public class CatalogAuditResetListener {
    @PreUpdate
    public void clearPreviousEditor(CatalogAuditMetadata metadata) {
        metadata.clearUpdatedByUserId();
    }
}
