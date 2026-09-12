package com.vnanalytics.checkpoint.repository;

import com.vnanalytics.checkpoint.entity.Checkpoint;
import com.vnanalytics.playthrough.entity.Playthrough;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckpointRepository
        extends JpaRepository<Checkpoint, Long> {

    Optional<Checkpoint> findByPlaythrough(
            Playthrough playthrough
    );
}
