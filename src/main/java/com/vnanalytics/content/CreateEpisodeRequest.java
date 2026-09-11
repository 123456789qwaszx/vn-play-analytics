package com.vnanalytics.content;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateEpisodeRequest(
        @NotBlank
        @Size(max = 50)
        String episodeKey,

        @NotBlank
        @Size(max = 100)
        String title,

        @Valid
        @NotNull
        @Size(min = 2)
        List<CreateChoiceOptionRequest> options
) {
}