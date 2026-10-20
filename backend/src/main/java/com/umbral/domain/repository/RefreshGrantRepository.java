package com.umbral.domain.repository;

import com.umbral.domain.entity.RefreshGrant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface RefreshGrantRepository extends JpaRepository<RefreshGrant, UUID> {
    Optional<RefreshGrant> findByTokenHash(String hash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from RefreshGrant g where g.id = :id")
    Optional<RefreshGrant> findLockedById(UUID id);
    @Modifying
    @Query("delete from RefreshGrant g where g.expiresAt < :cutoff")
    int deleteExpired(java.time.Instant cutoff);
}
