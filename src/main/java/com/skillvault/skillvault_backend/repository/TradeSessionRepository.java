package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.enums.TradeStatus;
import com.skillvault.skillvault_backend.model.TradeSession;
import com.skillvault.skillvault_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeSessionRepository extends JpaRepository<TradeSession, UUID> {

    List<TradeSession> findByRequester(User requester);

    List<TradeSession> findByProvider(User provider);

    List<TradeSession> findDistinctByRequesterOrProviderOrderByScheduledTimeDesc(User requester, User provider);
    List<TradeSession> findByProvider_Id(UUID providerId);
    List<TradeSession> findByRequester_Id(UUID requesterId);
    long countByProvider_IdAndStatus(UUID providerId, TradeStatus status);

}
