package com.vnanalytics.choice.repository;

import com.vnanalytics.choice.entity.ChoiceRecord;
import com.vnanalytics.playthrough.entity.Playthrough;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChoiceRecordRepository
        extends JpaRepository<ChoiceRecord, Long> {

    // 특정 회차의 현재 ChoiceRecord 전체 조회
    List<ChoiceRecord> findAllByPlaythroughOrderByIdAsc(
            Playthrough playthrough
    );

    // 특정 회차의 기존 ChoiceRecord 전체 삭제
    void deleteAllByPlaythrough(
            Playthrough playthrough
    );
}
