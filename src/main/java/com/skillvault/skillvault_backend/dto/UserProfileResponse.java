package com.skillvault.skillvault_backend.dto;

public record UserProfileResponse(
        UserProfileCoreDto user,
        UserProfileCreditsDto credits,
        UserProfileSkillsDto skills,
        UserProfileKnowledgeDto knowledge,
        UserProfileTradesDto trades
) {
}
