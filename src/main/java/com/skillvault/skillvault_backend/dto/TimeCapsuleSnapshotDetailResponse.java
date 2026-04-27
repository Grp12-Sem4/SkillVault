package com.skillvault.skillvault_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TimeCapsuleSnapshotDetailResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        Integer creditBalance,
        Integer totalTrades,
        Double averageRating,
        Integer skillCount,
        Integer knowledgeTopicCount,
        List<TimeCapsuleSkillSnapshotDto> skills,
        List<TimeCapsuleKnowledgeSnapshotDto> knowledgeTopics
) {
}
