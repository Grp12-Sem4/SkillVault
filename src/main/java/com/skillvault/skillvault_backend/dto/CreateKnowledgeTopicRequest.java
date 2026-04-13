package com.skillvault.skillvault_backend.dto;

public record CreateKnowledgeTopicRequest(
        String title,
        String subject,
        String content
) {
}
