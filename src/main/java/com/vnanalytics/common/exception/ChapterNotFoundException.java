package com.vnanalytics.common.exception;

public class ChapterNotFoundException extends RuntimeException {

    public ChapterNotFoundException(Long chapterId) {
        super("챕터를 찾을 수 없습니다: " + chapterId);
    }

    public ChapterNotFoundException(String chapterKey) {
        super("챕터를 찾을 수 없습니다: " + chapterKey);
    }
}