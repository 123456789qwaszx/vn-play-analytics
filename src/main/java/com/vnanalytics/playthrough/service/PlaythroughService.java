package com.vnanalytics.playthrough.service;

import com.vnanalytics.content.entity.Chapter;
import com.vnanalytics.content.repository.ChapterRepository;
import com.vnanalytics.playthrough.dto.CreatePlaythroughRequest;
import com.vnanalytics.playthrough.dto.PlaythroughResponse;
import com.vnanalytics.playthrough.entity.Playthrough;
import com.vnanalytics.playthrough.repository.PlaythroughRepository;

import java.time.Instant;
import java.util.Optional;

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

    public PlaythroughResponse createOrGet(
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

            return new PlaythroughResponse(
                    playthrough.getId(),
                    playthrough.getClientPlaythroughId()
            );
        }

        // 없으면 chapterKey로 Chapter 조회
        Chapter chapter = chapterRepository
                .findByChapterKey(request.chapterKey())
                .orElseThrow();

        // 새 Playthrough 저장
        Playthrough playthrough = playthroughRepository.save(
                new Playthrough(
                        chapter,
                        request.clientPlaythroughId(),
                        Instant.now()
                )
        );

        return new PlaythroughResponse(
                playthrough.getId(),
                playthrough.getClientPlaythroughId()
        );
    }
}
