package com.skillvault.skillvault_backend.dto;

import java.util.List;

public record TimeCapsuleComparisonResponse(
        TimeCapsuleSnapshotSummaryResponse oldSnapshot,
        TimeCapsuleSnapshotSummaryResponse newSnapshot,
        List<SkillSnapshotComparisonDto> commonSkills,
        List<KnowledgeSnapshotComparisonDto> commonKnowledgeTopics
) {
}
