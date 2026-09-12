package com.vnanalytics.playthrough.service;

import com.vnanalytics.common.exception.ChapterNotFoundException;
import com.vnanalytics.common.exception.PlaythroughChapterConflictException;
import com.vnanalytics.content.entity.Chapter;
import com.vnanalytics.content.repository.ChapterRepository;
import com.vnanalytics.playthrough.dto.CreatePlaythroughRequest;
import com.vnanalytics.playthrough.dto.PlaythroughRegistrationResult;
import com.vnanalytics.playthrough.dto.PlaythroughResponse;
import com.vnanalytics.playthrough.entity.Playthrough;
import com.vnanalytics.playthrough.repository.PlaythroughRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class PlaythroughService {

    private final PlaythroughRepository playthroughRepository;
    private final ChapterRepository chapterRepository;

    public PlaythroughService(
            PlaythroughRepository playthroughRepository,
            ChapterRepository chapterRepository
    ) {
        this.playthroughRepository = playthroughRepository;
        this.chapterRepository = chapterRepository;
    }

    @Transactional
    public PlaythroughRegistrationResult createOrGet(
            CreatePlaythroughRequest request
    ) {
        // clientPlaythroughId로 조회
        Optional<Playthrough> existing =
                playthroughRepository.findByClientPlaythroughId(
                        request.clientPlaythroughId()
                );

        // 있으면 기존 Playthrough 반환
        if (existing.isPresent()) {
            Playthrough playthrough = existing.get();

            String existingChapterKey =
                    playthrough.getChapter().getChapterKey();

            if (!existingChapterKey.equals(request.chapterKey())) {
                throw new PlaythroughChapterConflictException(
                        request.clientPlaythroughId(),
                        existingChapterKey,
                        request.chapterKey()
                );
            }

            return new PlaythroughRegistrationResult(
                    toResponse(playthrough),
                    false
            );
        }

        // 없으면 chapterKey로 Chapter 조회
        Chapter chapter = chapterRepository
                .findByChapterKey(request.chapterKey())
                .orElseThrow(
                        () -> new ChapterNotFoundException(request.chapterKey())
                );

        // 새 Playthrough 저장
        Playthrough playthrough = playthroughRepository.save(
                new Playthrough(
                        chapter,
                        request.clientPlaythroughId(),
                        Instant.now()
                )
        );

        return new PlaythroughRegistrationResult(
                toResponse(playthrough),
                true
        );
    }

    private PlaythroughResponse toResponse(Playthrough playthrough) {
        return new PlaythroughResponse(
                playthrough.getId(),
                playthrough.getClientPlaythroughId()
        );
    }
}
