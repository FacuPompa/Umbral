package com.umbral.web.controller;

import com.umbral.domain.dto.CatalogAuditResponse;
import com.umbral.domain.service.CatalogAuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/moderation")
@PreAuthorize("hasRole('MODERATOR')")
public class CatalogAuditController {
    private final CatalogAuditService catalogAuditService;

    public CatalogAuditController(CatalogAuditService catalogAuditService) {
        this.catalogAuditService = catalogAuditService;
    }

    @GetMapping("/games/{gameId}/audit")
    public CatalogAuditResponse gameAudit(@PathVariable Long gameId) {
        return catalogAuditService.getGameAudit(gameId);
    }

    @GetMapping("/checkpoints/{checkpointId}/audit")
    public CatalogAuditResponse checkpointAudit(@PathVariable Long checkpointId) {
        return catalogAuditService.getCheckpointAudit(checkpointId);
    }
}
