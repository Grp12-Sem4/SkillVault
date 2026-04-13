package com.skillvault.skillvault_backend.dto;

import com.skillvault.skillvault_backend.enums.UserRole;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        Integer creditBalance
) {
}
