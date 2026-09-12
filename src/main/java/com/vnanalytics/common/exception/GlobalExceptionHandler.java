package com.vnanalytics.common.exception;

import com.vnanalytics.content.exception.ChapterKeyConflictException;
import com.vnanalytics.content.exception.ContentValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCheckpointException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCheckpoint(
            InvalidCheckpointException e
    ) {
        return buildErrorResponse(
                ErrorCode.INVALID_CHECKPOINT,
                e.getMessage()
        );
    }

    @ExceptionHandler(PlaythroughNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlaythroughNotFound(
            PlaythroughNotFoundException e
    ) {
        return buildErrorResponse(
                ErrorCode.PLAYTHROUGH_NOT_FOUND,
                e.getMessage()
        );
    }

    @ExceptionHandler(PlaythroughChapterConflictException.class)
    public ResponseEntity<ErrorResponse> handlePlaythroughChapterConflict(
            PlaythroughChapterConflictException e
    ) {
        return buildErrorResponse(
                ErrorCode.PLAYTHROUGH_CHAPTER_CONFLICT,
                e.getMessage()
        );
    }

    @ExceptionHandler(ChapterNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChapterNotFound(
            ChapterNotFoundException e
    ) {
        return buildErrorResponse(
                ErrorCode.CHAPTER_NOT_FOUND,
                e.getMessage()
        );
    }

    @ExceptionHandler(ContentValidationException.class)
    public ResponseEntity<ErrorResponse> handleContentValidation(
            ContentValidationException e
    ) {
        return buildErrorResponse(
                ErrorCode.INVALID_CONTENT,
                e.getMessage()
        );
    }

    @ExceptionHandler(ChapterKeyConflictException.class)
    public ResponseEntity<ErrorResponse> handleChapterKeyConflict(
            ChapterKeyConflictException e
    ) {
        return buildErrorResponse(
                ErrorCode.CHAPTER_KEY_CONFLICT,
                e.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleRequestValidation(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");

        return buildErrorResponse(
                ErrorCode.VALIDATION_FAILED,
                message
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            ErrorCode errorCode,
            String message
    ) {
        ErrorResponse response = new ErrorResponse(
                errorCode,
                message
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }
}