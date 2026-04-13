package com.skillvault.skillvault_backend.dto;

import com.skillvault.skillvault_backend.enums.SkillType;

import java.util.UUID;

public record MarketplaceSkillResponse(
        UUID id,
        String title,
        String description,
        String category,
        Integer creditValue,
        SkillType type,
        String skillCategory,
        Double skillScore,
        Double confidenceIndex,
        UserSummaryResponse provider
) {
}
