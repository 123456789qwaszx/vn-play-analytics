package com.vnanalytics.playthrough.controller;

import com.vnanalytics.playthrough.dto.CreatePlaythroughRequest;
import com.vnanalytics.playthrough.dto.PlaythroughRegistrationResult;
import com.vnanalytics.playthrough.dto.PlaythroughResponse;
import com.vnanalytics.playthrough.service.PlaythroughService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/playthroughs")
public class PlaythroughController {

    private final PlaythroughService playthroughService;

    public PlaythroughController(
            PlaythroughService playthroughService
    ) {
        this.playthroughService = playthroughService;
    }

    @PostMapping
    public ResponseEntity<PlaythroughResponse> createOrGetPlaythrough (
            @Valid @RequestBody CreatePlaythroughRequest request
    ) {
        PlaythroughRegistrationResult result =
                playthroughService.createOrGet(request);

        // 기존 GUID 있음. 200 OK
        if(!result.created()) {
            return ResponseEntity.ok(result.playthrough());
        }

        // GUID 없을 경우
        // 저장 -> 201 Created + Location
        URI location = URI.create(
                "/playthroughs/" + result.playthrough().playthroughId()
        );

        return ResponseEntity
                .created(location)
                .body(result.playthrough());
    }
}