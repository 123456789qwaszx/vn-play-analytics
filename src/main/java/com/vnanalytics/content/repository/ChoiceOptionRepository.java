package com.vnanalytics.content.repository;

import com.vnanalytics.content.entity.ChoiceOption;
import com.vnanalytics.content.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChoiceOptionRepository extends JpaRepository<ChoiceOption, Long> {

    List<ChoiceOption> findAllByEpisodeOrderByOptionIndexAsc(Episode episode);

    Optional<ChoiceOption> findByEpisodeAndOptionIndex(
            Episode episode,
            Integer optionIndex
    );
}
