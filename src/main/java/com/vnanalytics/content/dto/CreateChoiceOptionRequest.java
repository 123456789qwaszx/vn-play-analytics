package com.vnanalytics.content.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateChoiceOptionRequest(
        @NotNull(message = "선택지 순번은 필수입니다.")
        @PositiveOrZero(message = "선택지 순번은 0 이상이어야 합니다.")
        Integer optionIndex,

        @NotNull(message = "선택지 문구는 필수입니다.")
        @Size(max = 200)
        String label,

        @NotNull(message = "자동 이동 여부는 필수입니다.")
        Boolean auto
) {
}