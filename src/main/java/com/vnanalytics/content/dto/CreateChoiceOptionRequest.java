package com.vnanalytics.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateChoiceOptionRequest(

        @NotNull(message = "선택지 순번은 필수입니다.")
        @PositiveOrZero(message = "선택지 순번은 0 이상이어야 합니다.")
        Integer optionIndex,

        @NotBlank
        @Size(max = 200)
        String label
) {
}