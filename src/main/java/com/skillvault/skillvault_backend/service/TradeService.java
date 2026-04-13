package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.enums.TradeStatus;
import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.model.TradeSession;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.SkillRepository;
import com.skillvault.skillvault_backend.repository.TradeSessionRepository;
import com.skillvault.skillvault_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import java.util.UUID;

@Service
public class TradeService {

    private final TradeSessionRepository tradeSessionRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final CreditService creditService;

    public TradeService(TradeSessionRepository tradeSessionRepository,
                        UserRepository userRepository,
                        SkillRepository skillRepository,
                        CreditService creditService) {
        this.tradeSessionRepository = tradeSessionRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.creditService = creditService;
    }

    public TradeSession createTradeRequest(TradeSession tradeSession) {
        if (tradeSession.getRequester() == null || tradeSession.getRequester().getId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Requester is required.");
        }

        if (tradeSession.getProvider() == null || tradeSession.getProvider().getId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Provider is required.");
        }

        if (tradeSession.getSkill() == null || tradeSession.getSkill().getId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Skill is required.");
        }

        User requester = userRepository.findById(tradeSession.getRequester().getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Requester not found"));
        User provider = userRepository.findById(tradeSession.getProvider().getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Provider not found"));
        Skill skill = skillRepository.findById(tradeSession.getSkill().getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Skill not found"));

        if (requester.getId().equals(provider.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Requester and provider cannot be the same user.");
        }

        if (skill.getUser() == null || !skill.getUser().getId().equals(provider.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Skill does not belong to provider.");
        }

        tradeSession.setRequester(requester);
        tradeSession.setProvider(provider);
        tradeSession.setSkill(skill);
        tradeSession.setStatus(TradeStatus.PENDING);

        return tradeSessionRepository.save(tradeSession);
    }

    public TradeSession acceptTrade(UUID tradeId) {
        TradeSession tradeSession = tradeSessionRepository.findById(tradeId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Trade not found"));

        if (tradeSession.getStatus() != TradeStatus.PENDING) {
            throw new ResponseStatusException(BAD_REQUEST, "Only pending trades can be accepted.");
        }

        tradeSession.setStatus(TradeStatus.ACTIVE);
        return tradeSessionRepository.save(tradeSession);
    }

    public TradeSession completeTrade(UUID tradeId, int rating) {
        TradeSession tradeSession = tradeSessionRepository.findById(tradeId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Trade not found"));

        if (tradeSession.getStatus() != TradeStatus.ACTIVE) {
            throw new ResponseStatusException(BAD_REQUEST, "Only active trades can be completed.");
        }

        creditService.transferCredits(tradeSession);
        tradeSession.setStatus(TradeStatus.COMPLETED);

        return tradeSessionRepository.save(tradeSession);
    }
}
