package com.umbral.domain.repository;

import com.umbral.domain.entity.UsedRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UsedRefreshTokenRepository extends JpaRepository<UsedRefreshToken, String> {}
