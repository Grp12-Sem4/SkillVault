package com.skillvault.skillvault_backend.dto;

public record UserProfileSkillsDto(
        long totalSkills,
        long technicalSkillsCount,
        long softSkillsCount,
        long offeredSkillsCount
) {
}
