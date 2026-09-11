package com.vnanalytics.content.repository;

import com.vnanalytics.content.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    boolean existsByChapterKey(String chapterKey);
}