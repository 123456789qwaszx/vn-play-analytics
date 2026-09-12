package com.vnanalytics.checkpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveCheckpointRequest(

        @NotBlank(message = "에피소드 키는 필수입니다.")
        @Size(max = 50, message = "에피소드 키는 50자를 넘을 수 없습니다.")
        String episodeKey,

        @NotNull(message = "챕터 완료 여부는 필수입니다.")
        Boolean chapterCompleted,

        @NotBlank(message = "스냅샷 JSON은 필수입니다.")
        String snapshotJson
) {
}
