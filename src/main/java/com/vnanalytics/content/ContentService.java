package com.vnanalytics.content;

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
    public Long createChapter(CreateChapterRequest request) {
        validateRequestDuplicates(request);

        if (chapterRepository.existsByCode(request.code())) {
            throw new ChapterCodeConflictException(request.code());
        }

        Chapter chapter = chapterRepository.save(
                new Chapter(request.code(), request.title())
        );

        for (CreateEpisodeRequest episodeRequest : request.episodes()) {
            Episode episode = episodeRepository.save(
                    new Episode(
                            chapter,
                            episodeRequest.code(),
                            episodeRequest.title()
                    )
            );

            List<ChoiceOption> options = episodeRequest.options().stream()
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

    private void validateRequestDuplicates(CreateChapterRequest request) {
        Set<String> episodeCodes = new HashSet<>();

        for (CreateEpisodeRequest episode : request.episodes()) {
            if (!episodeCodes.add(episode.code())) {
                throw new ContentValidationException(
                        "요청 안에서 장면 코드가 중복됩니다: " + episode.code()
                );
            }

            validateOptionIndexes(episode);
        }
    }

    private void validateOptionIndexes(CreateEpisodeRequest episode) {
        Set<Integer> optionIndexes = new HashSet<>();

        for (CreateChoiceOptionRequest option : episode.options()) {
            if (!optionIndexes.add(option.optionIndex())) {
                throw new ContentValidationException(
                        "장면 '%s' 안에서 선택지 순번이 중복됩니다: %d"
                                .formatted(
                                        episode.code(),
                                        option.optionIndex()
                                )
                );
            }
        }
    }
}