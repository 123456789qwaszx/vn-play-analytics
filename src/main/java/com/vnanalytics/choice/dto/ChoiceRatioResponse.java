package com.vnanalytics.choice.dto;

import java.math.BigDecimal;

public record ChoiceRatioResponse(
        String episodeKey,
        Integer optionIndex,
        String label,
        Long choiceCount,
        BigDecimal choiceRatio
) {
}
