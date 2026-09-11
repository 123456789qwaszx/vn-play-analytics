package com.vnanalytics.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateChapterRequest(
        @NotBlank(message = "챕터 키는 필수입니다.")
        @Size(max = 50, message = "챕터 키는 50자를 넘을 수 없습니다.")
        String chapterKey,

        @NotBlank
        @Size(max = 100)
        String title,

        @Valid
        @NotEmpty
        List<CreateEpisodeRequest> episodes
) {
}