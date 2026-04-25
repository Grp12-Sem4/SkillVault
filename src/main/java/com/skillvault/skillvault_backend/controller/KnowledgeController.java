package com.skillvault.skillvault_backend.controller;

import com.skillvault.skillvault_backend.dto.CreateKnowledgeTopicRequest;
import com.skillvault.skillvault_backend.dto.KnowledgeTopicResponse;
import com.skillvault.skillvault_backend.dto.ReviewKnowledgeTopicRequest;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.UserRepository;
import com.skillvault.skillvault_backend.service.KnowledgeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final UserRepository userRepository;

    public KnowledgeController(KnowledgeService knowledgeService, UserRepository userRepository) {
        this.knowledgeService = knowledgeService;
        this.userRepository = userRepository;
    }

    // Create topic
    @PostMapping
    public KnowledgeTopicResponse createTopic(@RequestBody CreateKnowledgeTopicRequest request, Principal principal) {
        return knowledgeService.createTopic(getAuthenticatedUser(principal), request);
    }

    @GetMapping
    public List<KnowledgeTopicResponse> getCurrentUserTopics(Principal principal) {
        return knowledgeService.getTopicsForUser(getAuthenticatedUser(principal));
    }

    // Review topic
    @PutMapping("/{topicId}/review")
    public KnowledgeTopicResponse reviewTopic(
            @PathVariable UUID topicId,
            @RequestBody(required = false) ReviewKnowledgeTopicRequest request
    ) {
        return request == null ? knowledgeService.reviewTopic(topicId) : knowledgeService.reviewTopic(topicId, request);
    }

    // Alternative review route that also accepts updated study notes or recall metadata.
    @PostMapping("/{topicId}/review")
    public KnowledgeTopicResponse reviewTopicWithContent(
            @PathVariable UUID topicId,
            @RequestBody(required = false) ReviewKnowledgeTopicRequest request
    ) {
        if (request == null) {
            return knowledgeService.reviewTopic(topicId);
        }

        return knowledgeService.reviewTopic(topicId, request);
    }

    // Apply decay to a topic
    @PutMapping("/{topicId}/decay")
    public KnowledgeTopicResponse applyDecay(@PathVariable UUID topicId) {
        return knowledgeService.applyDecay(topicId);
    }

    // Get topics needing revision
    @GetMapping({"/revision-needed", "/needs-revision"})
    public List<KnowledgeTopicResponse> getTopicsNeedingRevision(Principal principal) {
        return knowledgeService.getTopicsNeedingRevision(getAuthenticatedUser(principal));
    }

    // Get topics for a user
    @GetMapping("/user/{userId}")
    public List<KnowledgeTopicResponse> getTopicsByUser(@PathVariable UUID userId) {
        return knowledgeService.getTopicsByUser(userId);
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
