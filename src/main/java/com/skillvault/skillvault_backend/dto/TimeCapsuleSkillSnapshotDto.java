package com.skillvault.skillvault_backend.dto;

import java.util.UUID;

public record TimeCapsuleSkillSnapshotDto(
        UUID sourceSkillId,
        String skillName,
        Double skillScore,
        Double confidenceIndex,
        Integer masteryLevel,
        Integer practiceCount,
        Integer teachingCount,
        Integer usageFrequency
) {
}
