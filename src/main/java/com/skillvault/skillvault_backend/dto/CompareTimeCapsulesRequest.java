package com.skillvault.skillvault_backend.dto;

public record CompareTimeCapsulesRequest(
        Long oldSnapshotId,
        Long newSnapshotId
) {
}
