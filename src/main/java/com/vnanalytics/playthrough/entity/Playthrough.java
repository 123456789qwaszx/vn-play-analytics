package com.vnanalytics.playthrough.entity;

import com.vnanalytics.content.entity.Chapter;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "playthroughs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_playthrough_client_id",
                        columnNames = "client_playthrough_id"
                )
        }
)
public class Playthrough {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "chapter_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_playthrough_chapter")
    )
    private Chapter chapter;

    @Column(
            name = "client_playthrough_id",
            nullable = false,
            length = 32
    )
    private String clientPlaythroughId;

    @Column(
            name = "started_at",
            nullable = false
    )
    private Instant startedAt;

    protected Playthrough() {
    }

    public Playthrough(
            Chapter chapter,
            String clientPlaythroughId,
            Instant startedAt
    ) {
        this.chapter = chapter;
        this.clientPlaythroughId = clientPlaythroughId;
        this.startedAt = startedAt;
    }

    public Long getId() {
        return id;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public String getClientPlaythroughId() {
        return clientPlaythroughId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }
}