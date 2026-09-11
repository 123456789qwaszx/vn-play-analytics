package com.vnanalytics.content;

// 차후 400 Bad Request로 변환
public class ContentValidationException extends RuntimeException{

    public ContentValidationException(String message) {
        super(message);
    }
}
