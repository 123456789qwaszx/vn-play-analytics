package com.vnanalytics.choice.service;

import com.vnanalytics.choice.dto.ChoiceRatioResponse;
import com.vnanalytics.choice.repository.ChoiceStatisticsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChoiceStatisticsService {

    private final ChoiceStatisticsRepository choiceStatisticsRepository;

    private ChoiceStatisticsService(
            ChoiceStatisticsRepository choiceStatisticsRepository
    ) {
        this.choiceStatisticsRepository = choiceStatisticsRepository;
    }

    public List<ChoiceRatioResponse> getChoiceRatios() {
        return choiceStatisticsRepository
                .findChoiceRatios()
                .stream()
                .map(row -> new ChoiceRatioResponse(
                        row.getEpisodeKey(),
                        row.getOptionIndex(),
                        row.getLabel(),
                        row.getChoiceCount(),
                        row.getChoiceRatio()
                ))
                .toList();
    }
}
