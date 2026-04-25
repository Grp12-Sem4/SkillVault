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
        double retrievabilityScore,
        double decayRate,
        double stabilityDays,
        double easeFactor,
        double difficultyIndex,
        LocalDate nextReviewDate,
        int reviewCount,
        int successfulReviews,
        int consecutiveSuccessfulReviews,
        int lapseCount,
        int lastRecallScore,
        double averageRecallScore,
        LocalDate lastReviewed,
        LocalDate lastDecayCheck,
        LocalDateTime createdAt,
        boolean needsRevision
) {
}
