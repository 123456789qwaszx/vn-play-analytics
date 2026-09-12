package com.vnanalytics.choice.entity;

import com.vnanalytics.content.entity.ChoiceOption;
import com.vnanalytics.playthrough.entity.Playthrough;
import jakarta.persistence.*;

@Entity
@Table(name = "choice_records")
public class ChoiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "playthrough_id", // FK 컬럼을 뭐라고 부를지 , 관례: {참조테이블명}_id
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_choice_record_playthrough")
            // referencedColumnName를 생략하면 상대 PK를 자동으로 찾아감.
    )
    private Playthrough playthrough;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "choice_option_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_choice_record_choice_option")
    )
    private ChoiceOption choiceOption;

    protected ChoiceRecord() {
    }

    public ChoiceRecord(
            Playthrough playthrough,
            ChoiceOption choiceOption
    ) {
        this.playthrough = playthrough;
        this.choiceOption = choiceOption;
    }

    public Long getId() {
        return id;
    }

    public Playthrough getPlaythrough() {
        return playthrough;
    }

    public ChoiceOption getChoiceOption() {
        return choiceOption;
    }
}