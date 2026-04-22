package com.skillvault.skillvault_backend.dto;

public record UserProfileTradesDto(
        long totalTradeSessions,
        long completedTeachingSessions,
        int totalTeachingHours,
        int totalLearningHours
) {
}
