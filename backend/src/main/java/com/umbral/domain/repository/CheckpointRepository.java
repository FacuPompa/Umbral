package com.umbral.domain.repository;

import com.umbral.domain.entity.Checkpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckpointRepository extends JpaRepository<Checkpoint, Long> {
    public List<Checkpoint> findByGameIdOrderByPositionAsc (Long gameId);
}
