package com.vnanalytics.checkpoint.service;

import com.vnanalytics.checkpoint.dto.CheckpointResponse;
import com.vnanalytics.checkpoint.dto.SaveCheckpointRequest;
import com.vnanalytics.checkpoint.entity.Checkpoint;
import com.vnanalytics.checkpoint.repository.CheckpointRepository;
import com.vnanalytics.common.exception.InvalidCheckpointException;
import com.vnanalytics.common.exception.PlaythroughNotFoundException;
import com.vnanalytics.playthrough.entity.Playthrough;
import com.vnanalytics.playthrough.repository.PlaythroughRepository;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Service
public class CheckpointService {

    private final CheckpointRepository checkpointRepository;
    private final PlaythroughRepository playthroughRepository;
    private final JsonMapper jsonMapper;

    public CheckpointService(
            CheckpointRepository checkpointRepository,
            PlaythroughRepository playthroughRepository,
            JsonMapper jsonMapper
    ) {
        this.checkpointRepository = checkpointRepository;
        this.playthroughRepository = playthroughRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(readOnly = true)
    public CheckpointResponse getCheckpoint(Long playthroughId) {
        Playthrough playthrough = playthroughRepository
                .findById(playthroughId)
                .orElseThrow(
                        () -> new PlaythroughNotFoundException(playthroughId)
                );

        return checkpointRepository
                .findByPlaythrough(playthrough)
                .map(checkpoint -> this.toResponse(checkpoint))
                .orElse(null);
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

        validateSnapshot(playthrough, request);

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

    private void validateSnapshot(
            Playthrough playthrough,
            SaveCheckpointRequest request
    ) {
        JsonNode root;

        try {
            root = jsonMapper.readTree(request.snapshotJson());
        } catch (JacksonException e) {
            throw new InvalidCheckpointException(
                    "스냅샷 JSON 형식이 올바르지 않습니다."
            );
        }

        if(!root.isObject()) {
            throw new InvalidCheckpointException(
                    "스냅샷은 JSON 객체여야 합니다."
            );
        }

        String clientPlaythroughId =
                requireString(root, "playthroughId");

        String chapterKey =
                requireString(root, "chapterId");

        String episodeKey =
                requireString(root, "currentEpisodeId");

        boolean chapterCompleted =
                requireBoolean(root, "chapterCompleted");


        if (!clientPlaythroughId.equals(
                playthrough.getClientPlaythroughId())) {
            throw new InvalidCheckpointException(
                    "스냅샷의 회차 ID가 서버 회차와 일치하지 않습니다."
            );
        }

        if (!chapterKey.equals(
                playthrough.getChapter().getChapterKey())) {
            throw new InvalidCheckpointException(
                    "스냅샷의 챕터 키가 서버 회차와 일치하지 않습니다."
            );
        }

        if (!episodeKey.equals(request.episodeKey())) {
            throw new InvalidCheckpointException(
                    "스냅샷의 에피소드 키가 요청과 일치하지 않습니다."
            );
        }

        if (chapterCompleted
                != request.chapterCompleted().booleanValue()) {
            throw new InvalidCheckpointException(
                    "스냅샷의 챕터 완료 여부가 요청과 일치하지 않습니다."
            );
        }
    }

    private String requireString(
            JsonNode root,
            String fieldName
    ) {
        JsonNode node = root.path(fieldName);

        if (!node.isString()
                || node.stringValue().isBlank()) {
            throw new InvalidCheckpointException(
                    "스냅샷 필드가 올바르지 않습니다: " + fieldName
            );
        }

        return node.stringValue();
    }

    private boolean requireBoolean(
            JsonNode root,
            String fieldName
    ) {
        JsonNode node = root.path(fieldName);

        if (!node.isBoolean()) {
            throw new InvalidCheckpointException(
                    "스냅샷 필드가 올바르지 않습니다: " + fieldName
            );
        }

        return node.booleanValue();
    }
}
