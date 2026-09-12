package com.vnanalytics.choice.controller;

import com.vnanalytics.choice.dto.ReplaceChoicesRequest;
import com.vnanalytics.choice.service.ChoiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/playthroughs/{playthroughId}/choices")
public class ChoiceController {

    private final ChoiceService choiceService;

    public ChoiceController(ChoiceService choiceService) {
        this.choiceService = choiceService;
    }

    @PutMapping
    public ResponseEntity<Void> replaceChoices(
            @PathVariable Long playthroughId,
            @Valid @RequestBody ReplaceChoicesRequest request
    ) {
        choiceService.replaceChoices(
                playthroughId,
                request
        );

        return ResponseEntity.noContent().build();
    }
}
