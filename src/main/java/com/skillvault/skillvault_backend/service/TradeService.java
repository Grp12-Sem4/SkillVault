package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.dto.CreateTradeRequest;
import com.skillvault.skillvault_backend.dto.MarketplaceSkillResponse;
import com.skillvault.skillvault_backend.dto.TradeResponse;
import com.skillvault.skillvault_backend.dto.UserSummaryResponse;
import com.skillvault.skillvault_backend.enums.SkillType;
import com.skillvault.skillvault_backend.enums.TradeStatus;
import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.model.SoftSkill;
import com.skillvault.skillvault_backend.model.TechnicalSkill;
import com.skillvault.skillvault_backend.model.TradeSession;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.SkillRepository;
import com.skillvault.skillvault_backend.repository.TradeSessionRepository;
import com.skillvault.skillvault_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TradeService {

    private final TradeSessionRepository tradeSessionRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final CreditService creditService;
    private final SkillService skillService;

    public TradeService(TradeSessionRepository tradeSessionRepository,
                        UserRepository userRepository,
                        SkillRepository skillRepository,
                        CreditService creditService,
                        SkillService skillService) {
        this.tradeSessionRepository = tradeSessionRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.creditService = creditService;
        this.skillService = skillService;
    }

    public TradeResponse createTradeRequest(User requester, CreateTradeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Trade request body is required.");
        }

        if (request.providerId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Provider is required.");
        }

        if (request.skillId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Skill is required.");
        }

        if (request.scheduledTime() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Scheduled time is required.");
        }

        if (request.duration() == null || request.duration() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Duration must be greater than zero.");
        }

        User persistedRequester = userRepository.findById(requester.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Requester not found"));
        User provider = userRepository.findById(request.providerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Provider not found"));
        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Skill not found"));

        if (persistedRequester.getId().equals(provider.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Requester and provider cannot be the same user.");
        }

        if (skill.getUser() == null || !skill.getUser().getId().equals(provider.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Skill does not belong to provider.");
        }

        if (skill.getType() != SkillType.OFFERED) {
            throw new ResponseStatusException(BAD_REQUEST, "Only offered skills can be traded.");
        }

        TradeSession tradeSession = new TradeSession();
        tradeSession.setRequester(persistedRequester);
        tradeSession.setProvider(provider);
        tradeSession.setSkill(skill);
        tradeSession.setScheduledTime(request.scheduledTime());
        tradeSession.setDuration(request.duration());
        tradeSession.setStatus(TradeStatus.PENDING);
        tradeSession.setRating(null);

        return toTradeResponse(tradeSessionRepository.save(tradeSession));
    }

    public List<TradeResponse> getTradesForUser(User user) {
        return tradeSessionRepository.findDistinctByRequesterOrProviderOrderByScheduledTimeDesc(user, user).stream()
                .map(this::toTradeResponse)
                .toList();
    }

    public TradeResponse acceptTrade(UUID tradeId, User actingUser) {
        TradeSession tradeSession = tradeSessionRepository.findById(tradeId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Trade not found"));

        if (tradeSession.getProvider() == null || !tradeSession.getProvider().getId().equals(actingUser.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Only the provider may accept this trade.");
        }

        if (tradeSession.getStatus() != TradeStatus.PENDING) {
            throw new ResponseStatusException(BAD_REQUEST, "Only pending trades can be accepted.");
        }

        tradeSession.setStatus(TradeStatus.ACTIVE);
        return toTradeResponse(tradeSessionRepository.save(tradeSession));
    }

   public TradeResponse completeTrade(UUID tradeId, User actingUser, Integer rating) {
    TradeSession tradeSession = tradeSessionRepository.findById(tradeId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Trade not found"));

    if (tradeSession.getRequester() == null || !tradeSession.getRequester().getId().equals(actingUser.getId())) {
        throw new ResponseStatusException(FORBIDDEN, "Only the requester may complete this trade.");
    }

    if (tradeSession.getStatus() != TradeStatus.ACTIVE) {
        throw new ResponseStatusException(BAD_REQUEST, "Only active trades can be completed.");
    }

    if (rating == null || rating < 1 || rating > 5) {
        throw new ResponseStatusException(BAD_REQUEST, "Rating must be between 1 and 5.");
    }
    creditService.transferCredits(tradeSession);


    if (rating >= 4) {
        User provider = tradeSession.getProvider();
        Integer currentBalance = provider.getCreditBalance() != null ? provider.getCreditBalance() : 0;
        provider.setCreditBalance(currentBalance + 5);
        userRepository.save(provider);
    }

    Skill providerSkill = tradeSession.getSkill();
    User learner = tradeSession.getRequester();
    Skill originalSkill = tradeSession.getSkill();

    Skill learnerSkill = skillRepository.findByUserAndTitle(learner, originalSkill.getTitle()).orElse(null);

    if (learnerSkill == null) {
        Skill newSkill;

        if (originalSkill instanceof TechnicalSkill) {
            newSkill = new TechnicalSkill();
        } else {
            newSkill = new SoftSkill();
        }

        newSkill.setTitle(originalSkill.getTitle());
        newSkill.setDescription(originalSkill.getDescription());
        newSkill.setCategory(originalSkill.getCategory());
        newSkill.setType(SkillType.REQUESTED);
        newSkill.setUser(learner);
        newSkill.setMasteryLevel(10);
        newSkill.setConfidenceIndex(30.0);
        newSkill.setSkillScore(10.0);
        newSkill.setUsageFrequency(1);
        newSkill.setLastUsedAt(LocalDateTime.now());
        newSkill.setLastUsed(java.time.LocalDate.now());

        skillRepository.save(newSkill);
    } else {
        int currentMastery = learnerSkill.getMasteryLevel() != null ? learnerSkill.getMasteryLevel() : 0;
        learnerSkill.setMasteryLevel(Math.min(100, currentMastery + 5));
        learnerSkill.setUsageFrequency((learnerSkill.getUsageFrequency() != null ? learnerSkill.getUsageFrequency() : 0) + 1);
        learnerSkill.setLastUsedAt(LocalDateTime.now());
        learnerSkill.setLastUsed(java.time.LocalDate.now());

        skillRepository.save(learnerSkill);
    }

    if (providerSkill != null) {
        providerSkill.setTeachingCount((providerSkill.getTeachingCount() != null ? providerSkill.getTeachingCount() : 0) + 1);
        providerSkill.setUsageFrequency((providerSkill.getUsageFrequency() != null ? providerSkill.getUsageFrequency() : 0) + 1);

        double oldAvg = providerSkill.getAverageRating() != null ? providerSkill.getAverageRating() : 0.0;
        int count = providerSkill.getRatingCount() != null ? providerSkill.getRatingCount() : 0;
        double newAvg = ((oldAvg * count) + rating) / (count + 1);

        providerSkill.setAverageRating(newAvg);
        providerSkill.setRatingCount(count + 1);

        int currentMastery = providerSkill.getMasteryLevel() != null ? providerSkill.getMasteryLevel() : 0;

        // mastery improves based on teaching session
        int masteryIncrease = 5 + tradeSession.getDuration();
        providerSkill.setMasteryLevel(Math.min(100, currentMastery + masteryIncrease));

        providerSkill.setLastUsedAt(LocalDateTime.now());
        providerSkill.setLastUsed(java.time.LocalDate.now());

        // recalculate confidence after updating rating/teaching/usage
        providerSkill.setConfidenceIndex(skillService.calculateConfidence(providerSkill));

        skillRepository.save(providerSkill);
    }

    tradeSession.setRating(rating);
    tradeSession.setStatus(TradeStatus.COMPLETED);

    return toTradeResponse(tradeSessionRepository.save(tradeSession));
}

    private TradeResponse toTradeResponse(TradeSession tradeSession) {
        return new TradeResponse(
                tradeSession.getId(),
                tradeSession.getScheduledTime(),
                tradeSession.getDuration(),
                tradeSession.getStatus(),
                tradeSession.getRating(),
                skillToResponse(tradeSession.getSkill()),
                skillServiceUserSummary(tradeSession.getRequester()),
                skillServiceUserSummary(tradeSession.getProvider())
        );
    }

    private MarketplaceSkillResponse skillToResponse(Skill skill) {
        return skillService.toMarketplaceSkillResponse(skill);
    }

    private UserSummaryResponse skillServiceUserSummary(User user) {
        return skillService.toUserSummary(user);
    }
}
