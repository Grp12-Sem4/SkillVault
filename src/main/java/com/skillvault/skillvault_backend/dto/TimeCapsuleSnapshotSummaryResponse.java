package com.skillvault.skillvault_backend.dto;

import java.time.LocalDateTime;

public record TimeCapsuleSnapshotSummaryResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        Integer creditBalance,
        Integer totalTrades,
        Double averageRating,
        Integer skillCount,
        Integer knowledgeTopicCount
) {
}
