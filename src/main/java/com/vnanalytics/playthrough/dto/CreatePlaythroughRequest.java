package com.vnanalytics.playthrough.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlaythroughRequest(

        @NotBlank(message = "챕터 키는 필수입니다.")
        @Size(max = 50, message = "챕터 키는 50자를 넘을 수 없습니다.")
        String chapterKey,

        @NotBlank(message = "클라이언트 회차 ID는 필수입니다.")
        @Size(max = 32, message = "클라이언트 회차 ID는 32자를 넘을 수 없습니다.")
        String clientPlaythroughId
) {
}
