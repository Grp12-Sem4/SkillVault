package com.skillvault.skillvault_backend.dto;

import java.util.UUID;

public record KnowledgeSnapshotComparisonDto(
        UUID sourceTopicId,
        String topicName,
        Double oldMasteryLevel,
        Integer oldReviewCount,
        Double oldRetrievabilityScore,
        Double newMasteryLevel,
        Integer newReviewCount,
        Double newRetrievabilityScore
) {
}
