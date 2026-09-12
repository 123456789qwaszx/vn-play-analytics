package com.vnanalytics.choice.service;

import com.vnanalytics.choice.dto.ReplaceChoiceItemRequest;
import com.vnanalytics.choice.dto.ReplaceChoicesRequest;
import com.vnanalytics.choice.entity.ChoiceRecord;
import com.vnanalytics.choice.repository.ChoiceRecordRepository;
import com.vnanalytics.common.exception.InvalidChoiceException;
import com.vnanalytics.common.exception.PlaythroughNotFoundException;
import com.vnanalytics.content.entity.Chapter;
import com.vnanalytics.content.entity.ChoiceOption;
import com.vnanalytics.content.entity.Episode;
import com.vnanalytics.content.repository.ChoiceOptionRepository;
import com.vnanalytics.content.repository.EpisodeRepository;
import com.vnanalytics.playthrough.entity.Playthrough;
import com.vnanalytics.playthrough.repository.PlaythroughRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChoiceService {

    private final ChoiceRecordRepository choiceRecordRepository;
    private final PlaythroughRepository playthroughRepository;
    private final EpisodeRepository episodeRepository;
    private final ChoiceOptionRepository choiceOptionRepository;

    public ChoiceService(
            ChoiceRecordRepository choiceRecordRepository,
            PlaythroughRepository playthroughRepository,
            EpisodeRepository episodeRepository,
            ChoiceOptionRepository choiceOptionRepository
    ) {
        this.choiceRecordRepository = choiceRecordRepository;
        this.playthroughRepository = playthroughRepository;
        this.episodeRepository = episodeRepository;
        this. choiceOptionRepository = choiceOptionRepository;
    }

    @Transactional
    public void replaceChoices(
            Long playthroughId,
            ReplaceChoicesRequest request
    ) {
        Playthrough playthrough = playthroughRepository
                .findById(playthroughId)
                .orElseThrow(
                        () -> new PlaythroughNotFoundException(playthroughId)
                );

        Chapter chapter = playthrough.getChapter();

        List<ChoiceOption> choiceOptions = new ArrayList<>();

        for (ReplaceChoiceItemRequest choice : request.choices()) {
            Episode episode = episodeRepository
                    .findByChapterAndEpisodeKey(
                            chapter,
                            choice.episodeKey()
                    )
                    .orElseThrow(
                            () -> new InvalidChoiceException(
                                    "회차의 챕터에 존재하지 않는 에피소드입니다: "
                                    + choice.episodeKey()
                            )
                    );

            ChoiceOption choiceOption = choiceOptionRepository
                    .findByEpisodeAndOptionIndex(
                            episode,
                            choice.optionIndex()
                    )
                    .orElseThrow(
                            () -> new InvalidChoiceException(
                                    "에피소드에 존재하지 않는 선택지입니다: "
                                    + choice.episodeKey()
                                    + " / "
                                    + choice.optionIndex()
                            )
                    );

            choiceOptions.add(choiceOption);
        }

        choiceRecordRepository.deleteAllByPlaythrough(playthrough);

        List<ChoiceRecord> records = choiceOptions.stream()
                .map(choiceOption ->
                        new ChoiceRecord(playthrough, choiceOption)
                )
                .toList();

        choiceRecordRepository.saveAll(records);
    }
}
