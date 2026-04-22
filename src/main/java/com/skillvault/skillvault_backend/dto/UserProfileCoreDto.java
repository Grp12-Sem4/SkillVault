package com.skillvault.skillvault_backend.dto;

import com.skillvault.skillvault_backend.enums.AccountStatus;
import com.skillvault.skillvault_backend.enums.UserRole;

import java.util.UUID;

public record UserProfileCoreDto(
        UUID userId,
        String name,
        String email,
        UserRole role,
        AccountStatus accountStatus
) {
}
