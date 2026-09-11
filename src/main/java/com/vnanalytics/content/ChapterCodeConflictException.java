package com.vnanalytics.content;

// 차후 409 Conflict로 변환
public class ChapterCodeConflictException extends RuntimeException {

    public ChapterCodeConflictException(String code) {
        super("이미 등록된 챕터 코드입니다: " + code);
    }
}
