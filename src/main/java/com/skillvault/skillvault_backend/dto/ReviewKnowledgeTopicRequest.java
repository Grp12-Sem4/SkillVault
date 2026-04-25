package com.skillvault.skillvault_backend.dto;

public record ReviewKnowledgeTopicRequest(
        String updatedContent,
        Integer recallScore,
        Integer confidenceScore,
        Integer responseTimeSeconds
) {
}
