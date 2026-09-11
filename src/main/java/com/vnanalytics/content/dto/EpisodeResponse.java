package com.vnanalytics.content.dto;

public record EpisodeResponse(
        Long episodeId,
        String episodeKey,
        String title,
        List<ChoiceOptionResponse> options
) {
}
