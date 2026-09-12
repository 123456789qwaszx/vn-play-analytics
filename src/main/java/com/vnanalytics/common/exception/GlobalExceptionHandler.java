package com.vnanalytics.common.exception;

import com.vnanalytics.content.exception.ChapterKeyConflictException;
import com.vnanalytics.content.exception.ContentValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException e
    ) {
        return buildErrorResponse(
                ErrorCode.VALIDATION_FAILED,
                "필수 요청 파라미터가 누락되었습니다: " + e.getParameterName()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        return buildErrorResponse(
                ErrorCode.VALIDATION_FAILED,
                "요청 값의 자료형이 올바르지 않습니다: " + e.getName()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException e
    ) {
        return buildErrorResponse(
                ErrorCode.VALIDATION_FAILED,
                "요청 본문이 없거나 JSON 형식 또는 값의 자료형이 올바르지 않습니다."
        );
    }

    @ExceptionHandler(InvalidChoiceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidChoice(
            InvalidChoiceException e
    ) {
        return buildErrorResponse(
                ErrorCode.INVALID_CHOICE,
                e.getMessage()
        );
    }

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