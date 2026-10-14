package com.umbral.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "catalogAuditor", modifyOnCreate = false)
public class CatalogAuditingConfiguration {

    @Bean
    public AuditorAware<Long> catalogAuditor(EntityManager entityManager) {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }
            // A lifecycle listener must not trigger an automatic flush recursively.
            return entityManager.createQuery("select u.id from User u where u.handle = :handle", Long.class)
                    .setParameter("handle", authentication.getName())
                    .setFlushMode(FlushModeType.COMMIT)
                    .setMaxResults(1)
                    .getResultList().stream().findFirst();
        };
    }
}
