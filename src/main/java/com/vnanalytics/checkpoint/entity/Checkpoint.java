package com.vnanalytics.checkpoint.entity;

import com.vnanalytics.playthrough.entity.Playthrough;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "checkpoints")
public class Checkpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "playthrough_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_checkpoint_playthrough")
    )
    private Playthrough playthrough;

    @Column(
            name = "episode_key",
            nullable = false,
            length = 50
    )
    private String episodeKey;

    @Column(
            name = "chapter_completed",
            nullable = false
    )
    private boolean chapterCompleted;


    @Column(
            name = "snapshot_json",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String snapshotJson;

    @Column(
            name = "saved_at",
            nullable = false
    )
    private Instant savedAt;

    protected Checkpoint() {
    }

    public Checkpoint(
            Playthrough playthrough,
            String episodeKey,
            boolean chapterCompleted,
            String snapshotJson,
            Instant savedAt
    ) {
        this.playthrough = playthrough;
        this.episodeKey = episodeKey;
        this.chapterCompleted = chapterCompleted;
        this.snapshotJson = snapshotJson;
        this.savedAt = savedAt;
    }

    public Long getId() {
        return id;
    }

    public Playthrough getPlaythrough() {
        return playthrough;
    }

    public String getEpisodeKey() {
        return episodeKey;
    }

    public boolean isChapterCompleted() {
        return chapterCompleted;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public Instant getSavedAt() {
        return savedAt;
    }
}
