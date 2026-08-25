package com.umbral.domain.repository;

import com.umbral.domain.entity.UserGameProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGameProgressRepository extends JpaRepository<UserGameProgress, Long> {

    List<UserGameProgress> findAllByUserId(Long userId);

    Optional<UserGameProgress> findByUserIdAndGameId(Long userId, Long gameId);
}
