package com.skillvault.skillvault_backend.dto;

import java.util.UUID;

public record TimeCapsuleKnowledgeSnapshotDto(
        UUID sourceTopicId,
        String topicName,
        Double masteryLevel,
        Integer reviewCount,
        Double retrievabilityScore,
        String decayStatus,
        String revisionStatus
) {
}
