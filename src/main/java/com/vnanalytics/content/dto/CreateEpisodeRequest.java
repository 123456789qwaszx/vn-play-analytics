package com.vnanalytics.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateEpisodeRequest(
        @NotBlank(message = "장면 키는 필수입니다.")
        @Size(max = 50, message = "장면 키는 50자를 넘을 수 없습니다.")
        String episodeKey,

        @NotBlank
        @Size(max = 100)
        String title,

        @NotNull(message = "선택지 목록은 필수입니다.")
        List<@NotNull @Valid CreateChoiceOptionRequest> options
) {
}