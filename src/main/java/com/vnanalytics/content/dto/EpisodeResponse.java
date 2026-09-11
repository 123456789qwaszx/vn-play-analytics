package com.vnanalytics.content.dto;

import java.util.List;

public record EpisodeResponse(
        Long episodeId,
        String episodeKey,
        String title,
        List<ChoiceOptionResponse> options
) {
}
