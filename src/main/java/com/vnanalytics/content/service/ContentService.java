package com.vnanalytics.content.service;

import com.vnanalytics.content.exception.ChapterKeyConflictException;
import com.vnanalytics.content.exception.ContentValidationException;
import com.vnanalytics.content.dto.CreateChapterRequest;
import com.vnanalytics.content.dto.CreateChoiceOptionRequest;
import com.vnanalytics.content.dto.CreateEpisodeRequest;
import com.vnanalytics.content.entity.Chapter;
import com.vnanalytics.content.entity.ChoiceOption;
import com.vnanalytics.content.entity.Episode;
import com.vnanalytics.content.repository.ChapterRepository;
import com.vnanalytics.content.repository.ChoiceOptionRepository;
import com.vnanalytics.content.repository.EpisodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Long createChapter(CreateChapterRequest chapterRequest) {
        validateRequestDuplicates(chapterRequest);

        if (chapterRepository.existsByChapterKey(
                chapterRequest.chapterKey()
        )) {
            throw new ChapterKeyConflictException(
                    chapterRequest.chapterKey()
            );
        }

        Chapter chapter = chapterRepository.save(
                new Chapter(
                        chapterRequest.chapterKey(),
                        chapterRequest.title()
                )
        );

        for (CreateEpisodeRequest episodeRequest
                : chapterRequest.episodes()) {

            Episode episode = episodeRepository.save(
                    new Episode(
                            chapter,
                            episodeRequest.episodeKey(),
                            episodeRequest.title()
                    )
            );

            List<ChoiceOption> options =
                    episodeRequest.options().stream()
                            .map(optionRequest -> new ChoiceOption(
                                    episode,
                                    optionRequest.optionIndex(),
                                    optionRequest.label()
                            ))
                            .toList();

            choiceOptionRepository.saveAll(options);
        }

        return chapter.getId();
    }

    private void validateRequestDuplicates(
            CreateChapterRequest chapterRequest
    ) {
        Set<String> episodeKeys = new HashSet<>();

        for (CreateEpisodeRequest episodeRequest
                : chapterRequest.episodes()) {

            if (!episodeKeys.add(episodeRequest.episodeKey())) {
                throw new ContentValidationException(
                        "요청 안에서 장면 키가 중복됩니다: "
                                + episodeRequest.episodeKey()
                );
            }

            validateOptionIndexes(episodeRequest);
        }
    }

    private void validateOptionIndexes(
            CreateEpisodeRequest episodeRequest
    ) {
        Set<Integer> optionIndexes = new HashSet<>();

        for (CreateChoiceOptionRequest optionRequest
                : episodeRequest.options()) {

            if (!optionIndexes.add(optionRequest.optionIndex())) {
                throw new ContentValidationException(
                        "장면 '%s' 안에서 선택지 순번이 중복됩니다: %d"
                                .formatted(
                                        episodeRequest.episodeKey(),
                                        optionRequest.optionIndex()
                                )
                );
            }
        }
    }
}