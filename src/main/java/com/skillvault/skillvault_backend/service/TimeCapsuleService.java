package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.dto.CompareTimeCapsulesRequest;
import com.skillvault.skillvault_backend.dto.CreateTimeCapsuleSnapshotRequest;
import com.skillvault.skillvault_backend.dto.KnowledgeSnapshotComparisonDto;
import com.skillvault.skillvault_backend.dto.SkillSnapshotComparisonDto;
import com.skillvault.skillvault_backend.dto.TimeCapsuleComparisonResponse;
import com.skillvault.skillvault_backend.dto.TimeCapsuleKnowledgeSnapshotDto;
import com.skillvault.skillvault_backend.dto.TimeCapsuleSkillSnapshotDto;
import com.skillvault.skillvault_backend.dto.TimeCapsuleSnapshotDetailResponse;
import com.skillvault.skillvault_backend.dto.TimeCapsuleSnapshotSummaryResponse;
import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.model.TimeCapsuleKnowledge;
import com.skillvault.skillvault_backend.model.TimeCapsuleSkill;
import com.skillvault.skillvault_backend.model.TimeCapsuleSnapshot;
import com.skillvault.skillvault_backend.model.TradeSession;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.KnowledgeTopicRepository;
import com.skillvault.skillvault_backend.repository.SkillRepository;
import com.skillvault.skillvault_backend.repository.TimeCapsuleKnowledgeRepository;
import com.skillvault.skillvault_backend.repository.TimeCapsuleSkillRepository;
import com.skillvault.skillvault_backend.repository.TimeCapsuleSnapshotRepository;
import com.skillvault.skillvault_backend.repository.TradeSessionRepository;
import com.skillvault.skillvault_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TimeCapsuleService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final KnowledgeTopicRepository knowledgeTopicRepository;
    private final TradeSessionRepository tradeSessionRepository;
    private final TimeCapsuleSnapshotRepository timeCapsuleSnapshotRepository;
    private final TimeCapsuleSkillRepository timeCapsuleSkillRepository;
    private final TimeCapsuleKnowledgeRepository timeCapsuleKnowledgeRepository;

    public TimeCapsuleService(UserRepository userRepository,
                              SkillRepository skillRepository,
                              KnowledgeTopicRepository knowledgeTopicRepository,
                              TradeSessionRepository tradeSessionRepository,
                              TimeCapsuleSnapshotRepository timeCapsuleSnapshotRepository,
                              TimeCapsuleSkillRepository timeCapsuleSkillRepository,
                              TimeCapsuleKnowledgeRepository timeCapsuleKnowledgeRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.knowledgeTopicRepository = knowledgeTopicRepository;
        this.tradeSessionRepository = tradeSessionRepository;
        this.timeCapsuleSnapshotRepository = timeCapsuleSnapshotRepository;
        this.timeCapsuleSkillRepository = timeCapsuleSkillRepository;
        this.timeCapsuleKnowledgeRepository = timeCapsuleKnowledgeRepository;
    }

    @Transactional
    public TimeCapsuleSnapshotSummaryResponse createSnapshot(String userEmail, CreateTimeCapsuleSnapshotRequest request) {
        User user = getAuthenticatedUser(userEmail);
        String snapshotName = validateSnapshotName(request);

        List<Skill> currentSkills = skillRepository.findByUser(user);
        List<KnowledgeTopic> currentKnowledgeTopics =
                knowledgeTopicRepository.findByOwner_IdOrderByCreatedAtDesc(user.getId());
        List<TradeSession> trades =
                tradeSessionRepository.findDistinctByRequesterOrProviderOrderByScheduledTimeDesc(user, user);

        TimeCapsuleSnapshot snapshot = new TimeCapsuleSnapshot();
        snapshot.setUser(user);
        snapshot.setName(snapshotName);
        snapshot.setCreatedAt(LocalDateTime.now());
        snapshot.setCreditBalance(defaultInteger(user.getCreditBalance()));
        snapshot.setTotalTrades(trades.size());
        snapshot.setAverageRating(calculateAverageRating(trades, user));

        currentSkills.stream()
                .map(skill -> toSnapshotSkill(snapshot, skill))
                .forEach(snapshot::addSkill);

        currentKnowledgeTopics.stream()
                .map(topic -> toSnapshotKnowledge(snapshot, topic))
                .forEach(snapshot::addKnowledgeTopic);

        TimeCapsuleSnapshot savedSnapshot = timeCapsuleSnapshotRepository.save(snapshot);
        return toSummary(savedSnapshot);
    }

    public List<TimeCapsuleSnapshotSummaryResponse> getAllSnapshots(String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        return timeCapsuleSnapshotRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toSummary)
                .toList();
    }

    public TimeCapsuleSnapshotDetailResponse getSnapshot(String userEmail, Long snapshotId) {
        User user = getAuthenticatedUser(userEmail);
        TimeCapsuleSnapshot snapshot = getOwnedSnapshotOrThrow(snapshotId, user);
        List<TimeCapsuleSkill> skills = timeCapsuleSkillRepository.findBySnapshot_IdOrderBySkillNameAsc(snapshot.getId());
        List<TimeCapsuleKnowledge> knowledgeTopics =
                timeCapsuleKnowledgeRepository.findBySnapshot_IdOrderByTopicNameAsc(snapshot.getId());
        return toDetail(snapshot, skills, knowledgeTopics);
    }

    public TimeCapsuleComparisonResponse compareSnapshots(String userEmail, CompareTimeCapsulesRequest request) {
        User user = getAuthenticatedUser(userEmail);
        if (request == null || request.oldSnapshotId() == null || request.newSnapshotId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both snapshot IDs are required");
        }
        if (Objects.equals(request.oldSnapshotId(), request.newSnapshotId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select two different snapshots");
        }

        TimeCapsuleSnapshot oldSnapshot = getOwnedSnapshotOrThrow(request.oldSnapshotId(), user);
        TimeCapsuleSnapshot newSnapshot = getOwnedSnapshotOrThrow(request.newSnapshotId(), user);

        List<TimeCapsuleSkill> oldSkills =
                timeCapsuleSkillRepository.findBySnapshot_IdOrderBySkillNameAsc(oldSnapshot.getId());
        List<TimeCapsuleSkill> newSkills =
                timeCapsuleSkillRepository.findBySnapshot_IdOrderBySkillNameAsc(newSnapshot.getId());
        List<TimeCapsuleKnowledge> oldKnowledgeTopics =
                timeCapsuleKnowledgeRepository.findBySnapshot_IdOrderByTopicNameAsc(oldSnapshot.getId());
        List<TimeCapsuleKnowledge> newKnowledgeTopics =
                timeCapsuleKnowledgeRepository.findBySnapshot_IdOrderByTopicNameAsc(newSnapshot.getId());

        Map<String, TimeCapsuleSkill> newSkillsByKey = new LinkedHashMap<>();
        newSkills.forEach(skill -> newSkillsByKey.put(comparisonKey(skill.getSourceSkillId(), skill.getSkillName()), skill));

        List<SkillSnapshotComparisonDto> commonSkills = oldSkills.stream()
                .map(oldSkill -> {
                    String key = comparisonKey(oldSkill.getSourceSkillId(), oldSkill.getSkillName());
                    TimeCapsuleSkill newSkill = newSkillsByKey.get(key);
                    return newSkill == null ? null : toSkillComparison(oldSkill, newSkill);
                })
                .filter(Objects::nonNull)
                .toList();

        Map<String, TimeCapsuleKnowledge> newKnowledgeByKey = new LinkedHashMap<>();
        newKnowledgeTopics.forEach(topic ->
                newKnowledgeByKey.put(comparisonKey(topic.getSourceTopicId(), topic.getTopicName()), topic));

        List<KnowledgeSnapshotComparisonDto> commonKnowledgeTopics = oldKnowledgeTopics.stream()
                .map(oldTopic -> {
                    String key = comparisonKey(oldTopic.getSourceTopicId(), oldTopic.getTopicName());
                    TimeCapsuleKnowledge newTopic = newKnowledgeByKey.get(key);
                    return newTopic == null ? null : toKnowledgeComparison(oldTopic, newTopic);
                })
                .filter(Objects::nonNull)
                .toList();

        return new TimeCapsuleComparisonResponse(
                toSummary(oldSnapshot),
                toSummary(newSnapshot),
                commonSkills,
                commonKnowledgeTopics
        );
    }

    private User getAuthenticatedUser(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private TimeCapsuleSnapshot getOwnedSnapshotOrThrow(Long snapshotId, User user) {
        if (snapshotId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Snapshot ID is required");
        }

        return timeCapsuleSnapshotRepository.findByIdAndUser_Id(snapshotId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Snapshot not found"));
    }

    private String validateSnapshotName(CreateTimeCapsuleSnapshotRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Snapshot name is required");
        }

        return request.name().trim();
    }

    private TimeCapsuleSnapshotSummaryResponse toSummary(TimeCapsuleSnapshot snapshot) {
        return new TimeCapsuleSnapshotSummaryResponse(
                snapshot.getId(),
                snapshot.getName(),
                snapshot.getCreatedAt(),
                defaultInteger(snapshot.getCreditBalance()),
                defaultInteger(snapshot.getTotalTrades()),
                defaultDouble(snapshot.getAverageRating()),
                snapshot.getSkills() != null ? snapshot.getSkills().size() : 0,
                snapshot.getKnowledgeTopics() != null ? snapshot.getKnowledgeTopics().size() : 0
        );
    }

    private TimeCapsuleSnapshotDetailResponse toDetail(TimeCapsuleSnapshot snapshot,
                                                       List<TimeCapsuleSkill> skills,
                                                       List<TimeCapsuleKnowledge> knowledgeTopics) {
        List<TimeCapsuleSkillSnapshotDto> skillDtos = skills.stream()
                .map(this::toSkillSnapshotDto)
                .toList();
        List<TimeCapsuleKnowledgeSnapshotDto> knowledgeDtos = knowledgeTopics.stream()
                .map(this::toKnowledgeSnapshotDto)
                .toList();

        return new TimeCapsuleSnapshotDetailResponse(
                snapshot.getId(),
                snapshot.getName(),
                snapshot.getCreatedAt(),
                defaultInteger(snapshot.getCreditBalance()),
                defaultInteger(snapshot.getTotalTrades()),
                defaultDouble(snapshot.getAverageRating()),
                skillDtos.size(),
                knowledgeDtos.size(),
                skillDtos,
                knowledgeDtos
        );
    }

    private TimeCapsuleSkillSnapshotDto toSkillSnapshotDto(TimeCapsuleSkill skill) {
        return new TimeCapsuleSkillSnapshotDto(
                skill.getSourceSkillId(),
                skill.getSkillName(),
                defaultDouble(skill.getSkillScore()),
                defaultDouble(skill.getConfidenceIndex()),
                defaultInteger(skill.getMasteryLevel()),
                defaultInteger(skill.getPracticeCount()),
                defaultInteger(skill.getTeachingCount()),
                defaultInteger(skill.getUsageFrequency())
        );
    }

    private TimeCapsuleKnowledgeSnapshotDto toKnowledgeSnapshotDto(TimeCapsuleKnowledge topic) {
        return new TimeCapsuleKnowledgeSnapshotDto(
                topic.getSourceTopicId(),
                topic.getTopicName(),
                defaultDouble(topic.getMasteryLevel()),
                defaultInteger(topic.getReviewCount()),
                defaultDouble(topic.getRetrievabilityScore()),
                topic.getDecayStatus(),
                topic.getRevisionStatus()
        );
    }

    private TimeCapsuleSkill toSnapshotSkill(TimeCapsuleSnapshot snapshot, Skill skill) {
        TimeCapsuleSkill snapshotSkill = new TimeCapsuleSkill();
        snapshotSkill.setSnapshot(snapshot);
        snapshotSkill.setSourceSkillId(skill.getId());
        snapshotSkill.setSkillName(Optional.ofNullable(skill.getTitle()).orElse("Untitled Skill"));
        snapshotSkill.setSkillScore(defaultDouble(skill.getSkillScore()));
        snapshotSkill.setConfidenceIndex(defaultDouble(skill.getConfidenceIndex()));
        snapshotSkill.setMasteryLevel(defaultInteger(skill.getMasteryLevel()));
        snapshotSkill.setPracticeCount(defaultInteger(skill.getPracticeCount()));
        snapshotSkill.setTeachingCount(defaultInteger(skill.getTeachingCount()));
        snapshotSkill.setUsageFrequency(defaultInteger(skill.getUsageFrequency()));
        return snapshotSkill;
    }

    private TimeCapsuleKnowledge toSnapshotKnowledge(TimeCapsuleSnapshot snapshot, KnowledgeTopic topic) {
        TimeCapsuleKnowledge snapshotKnowledge = new TimeCapsuleKnowledge();
        snapshotKnowledge.setSnapshot(snapshot);
        snapshotKnowledge.setSourceTopicId(topic.getTopicId());
        snapshotKnowledge.setTopicName(Optional.ofNullable(topic.getTitle()).orElse("Untitled Topic"));
        snapshotKnowledge.setMasteryLevel(defaultDouble(topic.getMasteryLevel()));
        snapshotKnowledge.setReviewCount(defaultInteger(topic.getReviewCount()));
        snapshotKnowledge.setRetrievabilityScore(defaultDouble(topic.getRetrievabilityScore()));
        snapshotKnowledge.setDecayStatus(topic.getStatus() != null ? topic.getStatus().name() : "UNKNOWN");
        snapshotKnowledge.setRevisionStatus(topic.isReviewDue(java.time.LocalDate.now()) ? "NEEDS_REVIEW" : "ON_TRACK");
        return snapshotKnowledge;
    }

    private SkillSnapshotComparisonDto toSkillComparison(TimeCapsuleSkill oldSkill, TimeCapsuleSkill newSkill) {
        return new SkillSnapshotComparisonDto(
                oldSkill.getSourceSkillId() != null ? oldSkill.getSourceSkillId() : newSkill.getSourceSkillId(),
                firstNonBlank(oldSkill.getSkillName(), newSkill.getSkillName()),
                defaultDouble(oldSkill.getConfidenceIndex()),
                defaultInteger(oldSkill.getMasteryLevel()),
                defaultDouble(newSkill.getConfidenceIndex()),
                defaultInteger(newSkill.getMasteryLevel())
        );
    }

    private KnowledgeSnapshotComparisonDto toKnowledgeComparison(TimeCapsuleKnowledge oldTopic,
                                                                 TimeCapsuleKnowledge newTopic) {
        return new KnowledgeSnapshotComparisonDto(
                oldTopic.getSourceTopicId() != null ? oldTopic.getSourceTopicId() : newTopic.getSourceTopicId(),
                firstNonBlank(oldTopic.getTopicName(), newTopic.getTopicName()),
                defaultDouble(oldTopic.getMasteryLevel()),
                defaultInteger(oldTopic.getReviewCount()),
                defaultDouble(oldTopic.getRetrievabilityScore()),
                defaultDouble(newTopic.getMasteryLevel()),
                defaultInteger(newTopic.getReviewCount()),
                defaultDouble(newTopic.getRetrievabilityScore())
        );
    }

    private double calculateAverageRating(List<TradeSession> trades, User user) {
        List<Integer> ratings = trades.stream()
                .filter(trade -> trade.getRating() != null)
                .filter(trade -> trade.getProvider() != null && Objects.equals(trade.getProvider().getId(), user.getId()))
                .map(TradeSession::getRating)
                .toList();

        if (ratings.isEmpty()) {
            return defaultDouble(user.getReputationScore());
        }

        return ratings.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(defaultDouble(user.getReputationScore()));
    }

    private String comparisonKey(UUID sourceId, String name) {
        if (sourceId != null) {
            return sourceId.toString();
        }

        return normalizeName(name);
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String firstNonBlank(String left, String right) {
        if (left != null && !left.isBlank()) {
            return left;
        }
        return right;
    }

    private int defaultInteger(Integer value) {
        return value != null ? value : 0;
    }

    private double defaultDouble(Double value) {
        return value != null ? value : 0.0;
    }
}
