package com.umbral.domain.repository;

import com.umbral.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByHandle(String handle);

    boolean existsByHandle(String handle);

    boolean existsByEmail(String email);

    List<User> findTop5ByHandleContainingIgnoreCaseOrderByHandleAsc(String query);
}
