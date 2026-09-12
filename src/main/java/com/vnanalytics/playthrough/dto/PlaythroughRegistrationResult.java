package com.vnanalytics.playthrough.dto;

public record PlaythroughRegistrationResult(
        PlaythroughResponse playthrough,
        boolean created
) {
}