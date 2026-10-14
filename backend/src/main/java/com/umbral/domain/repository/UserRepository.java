package com.umbral.domain.repository;

import com.umbral.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByHandle(String handle);

    boolean existsByHandle(String handle);

    boolean existsByEmail(String email);

    List<User> findTop5ByHandleContainingIgnoreCaseOrderByHandleAsc(String query);

    @EntityGraph(attributePaths = "accountStatusChangedBy")
    Slice<User> findByHandleContainingIgnoreCaseOrderByHandleAscIdAsc(String query, Pageable pageable);

    @EntityGraph(attributePaths = "accountStatusChangedBy")
    Slice<User> findBySuspendedAndHandleContainingIgnoreCaseOrderByHandleAscIdAsc(
            boolean suspended, String query, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :userId")
    Optional<User> findForAccountModeration(Long userId);
}
