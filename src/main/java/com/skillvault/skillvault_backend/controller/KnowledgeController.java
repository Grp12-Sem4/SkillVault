package com.skillvault.skillvault_backend.controller;

import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import com.skillvault.skillvault_backend.service.KnowledgeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    // Create topic
    @PostMapping
    public KnowledgeTopic createTopic(@RequestBody KnowledgeTopic topic) {
        return knowledgeService.createTopic(topic);
    }

    // Review topic
    @PutMapping("/{topicId}/review")
    public KnowledgeTopic reviewTopic(@PathVariable UUID topicId) {
        return knowledgeService.reviewTopic(topicId);
    }

    // Alternative review route that also accepts updated study notes/content.
    @PostMapping("/{topicId}/review")
    public KnowledgeTopic reviewTopicWithContent(
            @PathVariable UUID topicId,
            @RequestBody(required = false) String updatedContent
    ) {
        if (updatedContent == null || updatedContent.isBlank()) {
            return knowledgeService.reviewTopic(topicId);
        }

        return knowledgeService.reviewTopic(topicId, updatedContent);
    }

    // Apply decay to a topic
    @PutMapping("/{topicId}/decay")
    public KnowledgeTopic applyDecay(@PathVariable UUID topicId) {
        return knowledgeService.applyDecay(topicId);
    }

    // Get topics needing revision
    @GetMapping({"/revision-needed", "/needs-revision"})
    public List<KnowledgeTopic> getTopicsNeedingRevision() {
        return knowledgeService.getTopicsNeedingRevision();
    }

    // Get topics for a user
    @GetMapping("/user/{userId}")
    public List<KnowledgeTopic> getTopicsByUser(@PathVariable UUID userId) {
        return knowledgeService.getTopicsByUser(userId);
    }
}
