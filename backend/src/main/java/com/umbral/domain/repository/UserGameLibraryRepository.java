package com.umbral.domain.repository;

import com.umbral.domain.entity.UserGameLibrary;
import com.umbral.domain.entity.UserGameLibraryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGameLibraryRepository extends JpaRepository<UserGameLibrary, Long> {
    List<UserGameLibrary> findAllByUserIdOrderByUpdatedAtDesc(Long userId);
    List<UserGameLibrary> findAllByUserIdAndFavoriteTrueOrderByUpdatedAtDesc(Long userId);
    Optional<UserGameLibrary> findByUserIdAndGameId(Long userId, Long gameId);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, UserGameLibraryStatus status);
}
