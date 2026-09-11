package com.vnanalytics.content.service;

import com.vnanalytics.common.exception.ChapterNotFoundException;
import com.vnanalytics.content.dto.*;
import com.vnanalytics.content.exception.ChapterKeyConflictException;
import com.vnanalytics.content.exception.ContentValidationException;
import com.vnanalytics.content.entity.Chapter;
import com.vnanalytics.content.entity.ChoiceOption;
import com.vnanalytics.content.entity.Episode;
import com.vnanalytics.content.repository.ChapterRepository;
import com.vnanalytics.content.repository.ChoiceOptionRepository;
import com.vnanalytics.content.repository.EpisodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ContentService {

    private final ChapterRepository chapterRepository;
    private final EpisodeRepository episodeRepository;
    private final ChoiceOptionRepository choiceOptionRepository;

    public ContentService(
            ChapterRepository chapterRepository,
            EpisodeRepository episodeRepository,
            ChoiceOptionRepository choiceOptionRepository
    ) {
        this.chapterRepository = chapterRepository;
        this.episodeRepository = episodeRepository;
        this.choiceOptionRepository = choiceOptionRepository;
    }

    public List<ChapterSummaryResponse> findChapters(String chapterKey) {
        return chapterRepository.findByChapterKey(chapterKey)
                .map(chapter ->
                        List.of(
                                new ChapterSummaryResponse(
                                        chapter.getId(),
                                        chapter.getChapterKey(),
                                        chapter.getTitle()
                                )
                        )
                )
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public ChapterDetailResponse getChapter(Long chapterId) {

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ChapterNotFoundException(chapterId));

        List<EpisodeResponse> episodes = episodeRepository
                .findAllByChapterOrderByIdAsc(chapter)
                .stream()
                .map(episode -> {
                    List<ChoiceOptionResponse> options = choiceOptionRepository
                            .findAllByEpisodeOrderByOptionIndexAsc(episode)
                            .stream()
                            .map(option ->
                                    new ChoiceOptionResponse(
                                            option.getOptionIndex(),
                                            option.getLabel(),
                                            option.isAuto()
                                    )
                            )
                            .toList();

                    return new EpisodeResponse(
                            episode.getId(),
                            episode.getEpisodeKey(),
                            episode.getTitle(),
                            options
                    );
                })
                .toList();

        return new ChapterDetailResponse(
                chapter.getId(),
                chapter.getChapterKey(),
                chapter.getTitle(),
                episodes
        );
    }

    @Transactional
    public Long createChapter(CreateChapterRequest chapterRequest) {
        // 요청 내부 중복 검사
        validateRequestDuplicates(chapterRequest);
        validateOptionLabels(chapterRequest);

        // DB에 같은 ChapterKey가 있는지 충돌 검사
        if (chapterRepository.existsByChapterKey(chapterRequest.chapterKey()))
            throw new ChapterKeyConflictException(chapterRequest.chapterKey());

        // Chapter 저장. DTO에서 받은 값을 Entity로 옮김.
        // 저장 후에는 chapter 객체에 DB가 만든 ID가 들어감.
        Chapter chapter =
                chapterRepository.save(
                        new Chapter(
                                chapterRequest.chapterKey(),
                                chapterRequest.title()
                        )
        );

        // Episode를 하나씩 저장.
        for (CreateEpisodeRequest episodeRequest : chapterRequest.episodes()) {
            Episode episode =
                    episodeRepository.save(
                            new Episode(
                                    chapter,
                                    episodeRequest.episodeKey(),
                                    episodeRequest.title()
                            )
                    );

            // CreateChoiceOptionRequest를 ChoiceOption으로 변환.
            List<ChoiceOption> options = new ArrayList<>();

            for (CreateChoiceOptionRequest optionRequest : episodeRequest.options()) {
                ChoiceOption option =
                        new ChoiceOption(
                                episode,
                                optionRequest.optionIndex(),
                                optionRequest.label(),
                                optionRequest.auto()
                        );

                options.add(option);
            }

            choiceOptionRepository.saveAll(options);
        }

        return chapter.getId();
    }

    private void validateRequestDuplicates(CreateChapterRequest chapter) {
        Set<String> seenEpisodeKeys  = new HashSet<>();

        for (CreateEpisodeRequest episode : chapter.episodes()) {
            if (!seenEpisodeKeys.add(episode.episodeKey()))
                throw new ContentValidationException("요청 안에서 장면 키가 중복됩니다: " + episode.episodeKey());

            validateOptionIndexes(episode);
        }
    }

    private void validateOptionIndexes(CreateEpisodeRequest episode) {
        Set<Integer> seenOptionIndexes = new HashSet<>();

        for (CreateChoiceOptionRequest option : episode.options()) {
            if (!seenOptionIndexes.add(option.optionIndex()))
                throw new ContentValidationException("장면 '%s' 안에서 선택지 순번이 중복됩니다: %d".formatted(episode.episodeKey(), option.optionIndex()));
        }
    }

    private void validateOptionLabels(CreateChapterRequest chapter) {
        for (CreateEpisodeRequest episode : chapter.episodes()) {
            for (CreateChoiceOptionRequest option : episode.options()) {
                if (!option.auto() && option.label().isBlank()) {
                    throw new ContentValidationException(
                            "에피소드 '%s'의 사용자 선택지 문구는 비어 있을 수 없습니다: %d"
                                    .formatted(
                                            episode.episodeKey(),
                                            option.optionIndex()
                                    )
                    );
                }
            }
        }
    }
}