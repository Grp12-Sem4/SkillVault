package com.skillvault.skillvault_backend.dto;

import java.time.LocalDateTime;

public record UserProfileCreditsDto(
        Integer currentCreditBalance,
        Integer totalCreditsEarned,
        Integer totalCreditsSpent,
        int creditTransactionCount,
        LocalDateTime lastCreditActivityDate
) {
}
