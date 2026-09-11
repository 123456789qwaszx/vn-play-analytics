package com.vnanalytics.content.controller;

import com.vnanalytics.content.dto.ChapterDetailResponse;
import com.vnanalytics.content.dto.ChapterSummaryResponse;
import com.vnanalytics.content.dto.CreateChapterRequest;
import com.vnanalytics.content.dto.CreateChapterResponse;
import com.vnanalytics.content.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/chapters")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public ResponseEntity<List<ChapterSummaryResponse>> findChapters(
            @RequestParam String chapterKey
    ) {
        List<ChapterSummaryResponse> response =
                contentService.findChapters(chapterKey);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{chapterId}")
    public ResponseEntity<ChapterDetailResponse> getChapter(
            @PathVariable Long chapterId
    ) {
        ChapterDetailResponse response =
                contentService.getChapter(chapterId);

        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<CreateChapterResponse> createChapter(
            @Valid @RequestBody CreateChapterRequest chapterRequest
    ) {
        Long chapterId = contentService.createChapter(chapterRequest);

        URI location = URI.create("/chapters/" + chapterId);

        return ResponseEntity
                .created(location)
                .body(new CreateChapterResponse(chapterId));
    }
}
