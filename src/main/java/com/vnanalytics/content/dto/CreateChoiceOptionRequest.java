package com.vnanalytics.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateChoiceOptionRequest(

        @NotNull
        @PositiveOrZero
        Integer optionIndex,

        @NotBlank
        @Size(max = 200)
        String label
) {
}