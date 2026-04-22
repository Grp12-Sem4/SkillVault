package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.dto.UserProfileCoreDto;
import com.skillvault.skillvault_backend.dto.UserProfileCreditsDto;
import com.skillvault.skillvault_backend.dto.UserProfileKnowledgeDto;
import com.skillvault.skillvault_backend.dto.UserProfileResponse;
import com.skillvault.skillvault_backend.dto.UserProfileSkillsDto;
import com.skillvault.skillvault_backend.dto.UserProfileTradesDto;
import com.skillvault.skillvault_backend.enums.CreditTransactionType;
import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import com.skillvault.skillvault_backend.enums.SkillType;
import com.skillvault.skillvault_backend.enums.TradeStatus;
import com.skillvault.skillvault_backend.model.CreditTransaction;
import com.skillvault.skillvault_backend.model.TradeSession;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.CreditTransactionRepository;
import com.skillvault.skillvault_backend.repository.KnowledgeRevisionHistoryRepository;
import com.skillvault.skillvault_backend.repository.KnowledgeTopicRepository;
import com.skillvault.skillvault_backend.repository.SkillRepository;
import com.skillvault.skillvault_backend.repository.TradeSessionRepository;
import com.skillvault.skillvault_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final SkillRepository skillRepository;
    private final KnowledgeTopicRepository knowledgeTopicRepository;
    private final KnowledgeRevisionHistoryRepository knowledgeRevisionHistoryRepository;
    private final TradeSessionRepository tradeSessionRepository;

    public UserProfileService(UserRepository userRepository,
                              CreditTransactionRepository creditTransactionRepository,
                              SkillRepository skillRepository,
                              KnowledgeTopicRepository knowledgeTopicRepository,
                              KnowledgeRevisionHistoryRepository knowledgeRevisionHistoryRepository,
                              TradeSessionRepository tradeSessionRepository) {
        this.userRepository = userRepository;
        this.creditTransactionRepository = creditTransactionRepository;
        this.skillRepository = skillRepository;
        this.knowledgeTopicRepository = knowledgeTopicRepository;
        this.knowledgeRevisionHistoryRepository = knowledgeRevisionHistoryRepository;
        this.tradeSessionRepository = tradeSessionRepository;
    }

    public UserProfileResponse getProfileByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        return buildProfile(user);
    }

    public UserProfileResponse getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        return buildProfile(user);
    }

    private UserProfileResponse buildProfile(User user) {
        UUID userId = user.getId();

        List<CreditTransaction> transactions = creditTransactionRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        List<TradeSession> teachingTrades = tradeSessionRepository.findByProvider_Id(userId);
        List<TradeSession> learningTrades = tradeSessionRepository.findByRequester_Id(userId);

        int totalCreditsEarned = sumTransactionsByType(transactions, CreditTransactionType.EARNED);
        int totalCreditsSpent = sumTransactionsByType(transactions, CreditTransactionType.SPENT);
        LocalDateTime lastCreditActivityDate = transactions.isEmpty() ? null : transactions.get(0).getCreatedAt();

        int totalTeachingHours = sumDurations(teachingTrades);
        int totalLearningHours = sumDurations(learningTrades);

        return new UserProfileResponse(
                new UserProfileCoreDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getAccountStatus()
                ),
                new UserProfileCreditsDto(
                        user.getCreditBalance(),
                        totalCreditsEarned,
                        totalCreditsSpent,
                        transactions.size(),
                        lastCreditActivityDate
                ),
                new UserProfileSkillsDto(
                        skillRepository.countByUser_Id(userId),
                        skillRepository.countTechnicalSkillsByUserId(userId),
                        skillRepository.countSoftSkillsByUserId(userId),
                        skillRepository.countByUser_IdAndType(userId, SkillType.OFFERED)
                ),
                new UserProfileKnowledgeDto(
                        knowledgeTopicRepository.countByOwner_IdAndStatus(userId, KnowledgeStatus.NEEDS_REVISION),
                        knowledgeRevisionHistoryRepository.countByTopic_Owner_Id(userId)
                ),
                new UserProfileTradesDto(
                        teachingTrades.size() + learningTrades.size(),
                        tradeSessionRepository.countByProvider_IdAndStatus(userId, TradeStatus.COMPLETED),
                        totalTeachingHours,
                        totalLearningHours
                )
        );
    }

    private int sumTransactionsByType(List<CreditTransaction> transactions, CreditTransactionType transactionType) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == transactionType)
                .map(CreditTransaction::getAmount)
                .filter(amount -> amount != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int sumDurations(List<TradeSession> trades) {
        return trades.stream()
                .map(TradeSession::getDuration)
                .filter(duration -> duration != null)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
