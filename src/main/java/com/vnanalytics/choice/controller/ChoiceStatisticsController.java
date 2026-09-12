package com.vnanalytics.choice.controller;

import com.vnanalytics.choice.dto.ChoiceRatioResponse;
import com.vnanalytics.choice.service.ChoiceStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statistics/choices")
public class ChoiceStatisticsController {

    private final ChoiceStatisticsService choiceStatisticsService;

    public ChoiceStatisticsController(
            ChoiceStatisticsService choiceStatisticsService
    ) {
        this.choiceStatisticsService = choiceStatisticsService;
    }

    @GetMapping
    public List<ChoiceRatioResponse> getChoiceRatio() {
        return choiceStatisticsService.getChoiceRatios();
    }
}
