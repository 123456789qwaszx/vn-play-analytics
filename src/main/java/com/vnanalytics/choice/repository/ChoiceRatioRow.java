package com.vnanalytics.choice.repository;

import java.math.BigDecimal;

public interface ChoiceRatioRow {

    String getEpisodeKey();

    Integer getOptionIndex();

    String getLabel();

    Long getChoiceCount();

    BigDecimal getChoiceRatio();
}
