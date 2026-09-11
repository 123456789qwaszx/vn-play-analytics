package com.vnanalytics.content.repository;

import com.vnanalytics.content.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    boolean existsByChapterKey(String chapterKey);

    Optional<Chapter> findByChapterKey(String chapterKey);
}