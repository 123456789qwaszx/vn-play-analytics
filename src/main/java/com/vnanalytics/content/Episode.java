package com.vnanalytics.content;

import jakarta.persistence.*;

@Entity
@Table(
        name = "episodes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_episode_chapter_code",
                        columnNames = {"chapter_id", "code"}
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

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String title;

    public Episode(Chapter chapter, String code, String title) {
        this.chapter = chapter;
        this.code = code;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }
}