package com.vnanalytics.content.repository;

import com.vnanalytics.content.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
}
