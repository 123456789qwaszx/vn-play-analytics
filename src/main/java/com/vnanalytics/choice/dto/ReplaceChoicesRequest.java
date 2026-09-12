package com.vnanalytics.choice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReplaceChoicesRequest (

        // '이 회차의 현재 확정 선택이 하나도 없다'라는 것도 유효.
        // "choices": []
        @NotNull(message = "선택 목록은 필수입니다.")
        List<@NotNull @Valid ReplaceChoiceItemRequest> choices
){
}
