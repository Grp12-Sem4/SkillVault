package com.skillvault.skillvault_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record KnowledgeTopicResponse(
        UUID id,
        String title,
        String subject,
        String content,
        String status,
        double masteryLevel,
        double decayRate,
        LocalDate lastReviewed,
        LocalDate lastDecayCheck,
        LocalDateTime createdAt,
        boolean needsRevision
) {
}
