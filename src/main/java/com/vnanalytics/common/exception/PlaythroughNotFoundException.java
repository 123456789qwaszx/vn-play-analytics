package com.vnanalytics.common.exception;

public class PlaythroughNotFoundException extends RuntimeException {

    public PlaythroughNotFoundException(Long playthroughId) {
        super("게임 회차를 찾을 수 없습니다: " + playthroughId);
    }
}
