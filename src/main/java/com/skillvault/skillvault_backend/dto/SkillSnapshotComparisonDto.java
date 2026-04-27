package com.skillvault.skillvault_backend.dto;

import java.util.UUID;

public record SkillSnapshotComparisonDto(
        UUID sourceSkillId,
        String skillName,
        Double oldConfidenceIndex,
        Integer oldMasteryLevel,
        Double newConfidenceIndex,
        Integer newMasteryLevel
) {
}
