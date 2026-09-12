package com.vnanalytics.checkpoint.service;

import com.vnanalytics.checkpoint.dto.CheckpointResponse;
import com.vnanalytics.checkpoint.dto.SaveCheckpointRequest;
import com.vnanalytics.checkpoint.entity.Checkpoint;
import com.vnanalytics.checkpoint.repository.CheckpointRepository;
import com.vnanalytics.common.exception.PlaythroughNotFoundException;
import com.vnanalytics.playthrough.entity.Playthrough;
import com.vnanalytics.playthrough.repository.PlaythroughRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CheckpointService {

    private final CheckpointRepository checkpointRepository;
    private final PlaythroughRepository playthroughRepository;

    public CheckpointService(
            CheckpointRepository checkpointRepository,
            PlaythroughRepository playthroughRepository
    ) {
        this.checkpointRepository = checkpointRepository;
        this.playthroughRepository = playthroughRepository;
    }

    @Transactional
    public CheckpointResponse saveCheckpoint(
            Long playthroughId,
            SaveCheckpointRequest request
    ) {
        Playthrough playthrough = playthroughRepository
                .findById(playthroughId)
                .orElseThrow(
                        () -> new PlaythroughNotFoundException(playthroughId)
                );

        Instant now = Instant.now();

        Checkpoint checkpoint = checkpointRepository
                .findByPlaythrough(playthrough)
                .orElse(null);

        if (checkpoint == null) {
            checkpoint = checkpointRepository.save(
                    new Checkpoint(
                            playthrough,
                            request.episodeKey(),
                            request.chapterCompleted(),
                            request.snapshotJson(),
                            now
                    )
            );
        } else {
            checkpoint.update(// save없어도 트랙잭션 안이라 DB가 갱신 됨.
                    request.episodeKey(),
                    request.chapterCompleted(),
                    request.snapshotJson(),
                    now
            );
        }

        return toResponse(checkpoint);
    }

    private CheckpointResponse toResponse(Checkpoint checkpoint) {
        return new CheckpointResponse(
                checkpoint.getId(),
                checkpoint.getPlaythrough().getId(),
                checkpoint.getEpisodeKey(),
                checkpoint.isChapterCompleted(),
                checkpoint.getSnapshotJson(),
                checkpoint.getSavedAt()
        );
    }
}
