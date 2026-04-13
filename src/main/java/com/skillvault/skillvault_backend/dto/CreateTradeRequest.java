package com.skillvault.skillvault_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTradeRequest(
        UUID providerId,
        UUID skillId,
        LocalDateTime scheduledTime,
        Integer duration
) {
}
