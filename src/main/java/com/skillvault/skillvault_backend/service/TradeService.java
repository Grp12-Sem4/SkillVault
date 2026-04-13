
package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.enums.TradeStatus;
import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.model.TradeSession;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.SkillRepository;
import com.skillvault.skillvault_backend.repository.TradeSessionRepository;
import com.skillvault.skillvault_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TradeService {
    private final TradeSessionRepository tradeSessionRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final CreditService creditService;

    public TradeService(TradeSessionRepository tradeSessionRepository,UserRepository userRepository,SkillRepository skillRepository,CreditService creditService) {
        this.tradeSessionRepository = tradeSessionRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.creditService = creditService;
    }

    public TradeSession createTradeRequest(UUID requesterId,UUID providerId,UUID skillId,LocalDateTime scheduledTime,Integer duration) {

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (requester.getId().equals(provider.getId())) {
            throw new RuntimeException("Requester and provider cannot be the same user.");
        }

        if (!skill.getUser().getId().equals(provider.getId())) {
            throw new RuntimeException("Skill does not belong to provider.");
        }

        TradeSession tradeSession = new TradeSession();
        tradeSession.setRequester(requester);
        tradeSession.setProvider(provider);
        tradeSession.setSkill(skill);
        tradeSession.setScheduledTime(scheduledTime);
        tradeSession.setDuration(duration);
        tradeSession.setStatus(TradeStatus.PENDING);

        return tradeSessionRepository.save(tradeSession);
    }

    public TradeSession acceptTrade(UUID tradeId) {
        TradeSession tradeSession = tradeSessionRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found"));

        if (tradeSession.getStatus() != TradeStatus.PENDING) {
            throw new RuntimeException("Only pending trades can be accepted.");
        }

        tradeSession.setStatus(TradeStatus.ACTIVE);
        return tradeSessionRepository.save(tradeSession);
    }

    public TradeSession completeTrade(UUID tradeId, int rating) {
        TradeSession tradeSession = tradeSessionRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found"));

        if (tradeSession.getStatus() != TradeStatus.ACTIVE) {
            throw new RuntimeException("Only active trades can be completed.");
        }

        creditService.transferCredits(tradeSession);
        tradeSession.setStatus(TradeStatus.COMPLETED);

        return tradeSessionRepository.save(tradeSession);
    }

    public List<TradeSession> getTradesByUser(UUID userId) {
        return tradeSessionRepository.findAll().stream()
                .filter(t -> t.getRequester().getId().equals(userId)
                        || t.getProvider().getId().equals(userId))
                .toList();
    }

    public TradeSession createTradeRequest(TradeSession tradeSession) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createTradeRequest'");
    }
}
