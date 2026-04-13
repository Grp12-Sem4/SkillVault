package com.skillvault.skillvault_backend.dto;

import com.skillvault.skillvault_backend.enums.TradeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TradeResponse(
        UUID id,
        LocalDateTime scheduledTime,
        Integer duration,
        TradeStatus status,
        Integer rating,
        MarketplaceSkillResponse skill,
        UserSummaryResponse requester,
        UserSummaryResponse provider
) {
}
