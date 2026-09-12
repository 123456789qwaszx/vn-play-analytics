package com.vnanalytics.choice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ReplaceChoiceItemRequest(

        @NotBlank(message = "에피소드 키는 필수입니다.")
        @Size(max = 50, message = "에피소드 키는 50자를 넘을 수 없습니다.")
        String episodeKey,

        @NotNull(message = "선택지 순번은 필수입니다.")
        @PositiveOrZero(message = "선택지 순번은 0 이상이어야 합니다.")
        Integer optionIndex
) {
}
