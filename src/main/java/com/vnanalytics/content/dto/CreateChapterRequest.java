package com.vnanalytics.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateChapterRequest(
        @NotBlank
        @Size(max = 50)
        String chapterKey,

        @NotBlank
        @Size(max = 100)
        String title,

        @Valid
        @NotEmpty
        List<CreateEpisodeRequest> episodes
) {
}