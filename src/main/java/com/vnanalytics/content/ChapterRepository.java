package com.vnanalytics.content;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    boolean existsByCode(String code);
}