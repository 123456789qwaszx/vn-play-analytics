package com.vnanalytics.content.repository;

import com.vnanalytics.content.entity.Chapter;
import com.vnanalytics.content.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    List<Episode> findAllByChapterOrderByIdAsc(Chapter chapter);
}
