package com.vnanalytics.content.dto;

import java.util.List;

public record ChapterDetailResponse(
    Long chapterId,
    String chapterKey,
    String title,
    List<EpisodeResponse> episodes
) {
}