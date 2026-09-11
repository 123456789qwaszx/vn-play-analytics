package com.vnanalytics.content.exception;

public class ChapterKeyConflictException extends RuntimeException {

    public ChapterKeyConflictException(String chapterKey) {
        super("이미 등록된 챕터 키입니다: " + chapterKey);
    }
}