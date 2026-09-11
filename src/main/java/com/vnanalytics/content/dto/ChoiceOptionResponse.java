package com.vnanalytics.content.dto;

public record ChoiceOptionResponse(
        Integer optionIndex,
        String label,
        boolean auto
) {
}