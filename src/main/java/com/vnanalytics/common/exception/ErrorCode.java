package com.vnanalytics.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    INVALID_CONTENT(HttpStatus.BAD_REQUEST),

    CHAPTER_KEY_CONFLICT(HttpStatus.CONFLICT),
    PLAYTHROUGH_CHAPTER_CONFLICT(HttpStatus.CONFLICT),

    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND),
    PLAYTHROUGH_NOT_FOUND(HttpStatus.NOT_FOUND),

    INVALID_CHECKPOINT(HttpStatus.BAD_REQUEST),

    INVALID_CHOICE(HttpStatus.BAD_REQUEST);


    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}