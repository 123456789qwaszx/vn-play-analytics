package com.vnanalytics.checkpoint.dto;

import java.time.Instant;

public record CheckpointResponse(
        Long checkpointId,
        Long playthroughId,
        String episodeKey,
        boolean chapterCompleted,
        String snapshotJson,
        Instant savedAt
) {
}
