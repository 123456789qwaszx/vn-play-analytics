package com.vnanalytics.content.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "episodes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_episode_chapter_key",
                        columnNames = {"chapter_id", "episode_key"}
                )
        }
)
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "chapter_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_episode_chapter")
    )
    private Chapter chapter;

    @Column(name = "episode_key", nullable = false, length = 50)
    private String episodeKey;

    @Column(nullable = false, length = 100)
    private String title;

    protected Episode() {
    }

    public Episode(
            Chapter chapter,
            String episodeKey,
            String title
    ) {
        this.chapter = chapter;
        this.episodeKey = episodeKey;
        this.title = title;
    }

    public String getEpisodeKey() {
        return episodeKey;
    }
}