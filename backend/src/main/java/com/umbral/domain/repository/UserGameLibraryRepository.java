package com.umbral.domain.repository;

import com.umbral.domain.entity.UserGameLibrary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGameLibraryRepository extends JpaRepository<UserGameLibrary, Long> {
    List<UserGameLibrary> findAllByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<UserGameLibrary> findByUserIdAndGameId(Long userId, Long gameId);
}
