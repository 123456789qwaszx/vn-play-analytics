package com.vnanalytics.checkpoint.controller;

import com.vnanalytics.checkpoint.dto.CheckpointResponse;
import com.vnanalytics.checkpoint.dto.SaveCheckpointRequest;
import com.vnanalytics.checkpoint.service.CheckpointService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/playthroughs/{playthroughId}/checkpoint")
public class CheckpointController {

    private final CheckpointService checkpointService;

    public CheckpointController(
            CheckpointService checkpointService
    ) {
        this.checkpointService = checkpointService;
    }

    @PutMapping
    public ResponseEntity<CheckpointResponse> saveCheckpoint(
            @PathVariable Long playthroughId,
            @Valid @RequestBody SaveCheckpointRequest request
    ) {
        CheckpointResponse response =
                checkpointService.saveCheckpoint(
                        playthroughId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}
