package com.vnanalytics.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "choice_options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_choice_option_episode_index",
                        columnNames = {"episode_id", "option_index"}
                )
        }
)
public class ChoiceOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "episode_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_choice_option_episode")
    )
    private Episode episode;

    @Column(name = "option_index", nullable = false)
    private Integer optionIndex;

    @Column(nullable = false, length = 200)
    private String label;

    protected ChoiceOption() {
    }

    public ChoiceOption(Episode episode, Integer optionIndex, String label) {
        this.episode = episode;
        this.optionIndex = optionIndex;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public Episode getEpisode() {
        return episode;
    }

    public Integer getOptionIndex() {
        return optionIndex;
    }

    public String getLabel() {
        return label;
    }
}