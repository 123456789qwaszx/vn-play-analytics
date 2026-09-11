package com.vnanalytics.content.repository;

import com.vnanalytics.content.entity.ChoiceOption;
import com.vnanalytics.content.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChoiceOptionRepository extends JpaRepository<ChoiceOption, Long> {

    List<ChoiceOption> findAllByEpisodeOrderByOptionIndexAsc(Episode episode);
}
