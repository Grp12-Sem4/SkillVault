package com.skillvault.skillvault_backend.dto;

public record UserProfileKnowledgeDto(
        long topicsNeedingImmediateRevision,
        long totalRevisionEvents
) {
}
