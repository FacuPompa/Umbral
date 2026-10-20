package com.umbral.config;

import com.umbral.domain.repository.RefreshGrantRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Configuration
@EnableScheduling
public class RefreshGrantMaintenance {
    private final RefreshGrantRepository grants;
    public RefreshGrantMaintenance(RefreshGrantRepository grants) { this.grants = grants; }
    // Keep used-token hashes until the family's absolute expiry so replay detection remains effective.
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 3_600_000)
    @Transactional
    public void removeExpired() { grants.deleteExpired(Instant.now()); }
}
