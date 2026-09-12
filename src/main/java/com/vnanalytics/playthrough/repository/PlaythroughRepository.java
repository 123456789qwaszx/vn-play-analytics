package com.vnanalytics.playthrough.repository;

import com.vnanalytics.playthrough.entity.Playthrough;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaythroughRepository
        extends JpaRepository<Playthrough, Long> {

    Optional<Playthrough> findByClientPlaythroughId(
            String clientPlaythroughId
    );
}
