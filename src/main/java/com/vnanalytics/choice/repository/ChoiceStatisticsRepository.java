package com.vnanalytics.choice.repository;

import com.vnanalytics.content.entity.ChoiceOption;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ChoiceStatisticsRepository
        extends Repository<ChoiceOption, Long> {

    @Query(value = """
            SELECT
                e.episode_key AS episodeKey,
                co.option_index AS optionIndex,
                co.label AS label,
                COUNT(cr.id) AS choiceCount,
                ROUND(
                    COUNT(cr.id) * 100.0
                    / NULLIF(
                        SUM(COUNT(cr.id)) OVER (
                            PARTITION BY e.id
                        ),
                        0
                    ),
                    1
                ) AS choiceRatio
            FROM choice_options co
            JOIN episodes e
                ON co.episode_id = e.id
            LEFT JOIN choice_records cr
                ON cr.choice_option_id = co.id
            WHERE co.is_auto = 0
              AND e.id IN (
                  SELECT episode_id
                  FROM choice_options
                  WHERE is_auto = 0
                  GROUP BY episode_id
                  HAVING COUNT(*) >= 2
              )
            GROUP BY
                e.id,
                e.episode_key,
                co.id,
                co.option_index,
                co.label
            ORDER BY
                e.id,
                co.option_index
            """,
            nativeQuery = true)
    List<ChoiceRatioRow> findChoiceRatios();
}