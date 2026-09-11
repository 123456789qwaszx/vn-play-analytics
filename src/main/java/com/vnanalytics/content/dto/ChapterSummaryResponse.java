package com.vnanalytics.content.dto;

public record ChapterSummaryResponse(
        Long chapterId,
        String chapterKey,
        String title
) {
}
