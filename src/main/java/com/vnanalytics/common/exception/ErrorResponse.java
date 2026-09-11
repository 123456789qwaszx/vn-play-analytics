package com.vnanalytics.common.exception;

public record ErrorResponse(
        ErrorCode errorCode,
        String message
) {
}