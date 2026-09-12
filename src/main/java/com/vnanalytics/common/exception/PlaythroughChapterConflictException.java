package com.vnanalytics.common.exception;

public class PlaythroughChapterConflictException extends RuntimeException {

    public PlaythroughChapterConflictException(
            String clientPlaythroughId,
            String existingChapterKey,
            String requestedChapterKey
    ) {
        super(
                "같은 클라이언트 회차 ID가 다른 챕터에 등록되어 있습니다. " +
                        "clientPlaythroughId=%s, existing=%s, requested=%s"
                                .formatted(
                                        clientPlaythroughId,
                                        existingChapterKey,
                                        requestedChapterKey
                                )
        );
    }
}
