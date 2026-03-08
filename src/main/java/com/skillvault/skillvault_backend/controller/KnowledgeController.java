package com.skillvault.skillvault_backend.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import com.skillvault.skillvault_backend.service.KnowledgeService;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    // Call this to "Study" a topic. It resets mastery to 100% and logs history.
    @PostMapping("/{id}/review")
    public ResponseEntity<KnowledgeTopic> reviewTopic(
            @PathVariable UUID id, 
            @RequestBody String updatedContent) {
        return ResponseEntity.ok(knowledgeService.reviewTopic(id, updatedContent));
    }
}