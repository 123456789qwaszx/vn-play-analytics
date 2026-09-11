package com.vnanalytics.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "chapters",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chapter_key",
                        columnNames = "chapter_key"
                )
        }
)
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_key", nullable = false, length = 50)
    private String chapterKey;

    @Column(nullable = false, length = 100)
    private String title;

    protected Chapter() {
    }

    public Chapter(String chapterKey, String title) {
        this.chapterKey = chapterKey;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getChapterKey() {
        return chapterKey;
    }

    public String getTitle() {
        return title;
    }
}