package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.dto.CreateKnowledgeTopicRequest;
import com.skillvault.skillvault_backend.dto.KnowledgeTopicResponse;
import com.skillvault.skillvault_backend.dto.ReviewKnowledgeTopicRequest;
import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import com.skillvault.skillvault_backend.model.KnowledgeRevisionHistory;
import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.KnowledgeRevisionHistoryRepository;
import com.skillvault.skillvault_backend.repository.KnowledgeTopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeService {

    private final KnowledgeTopicRepository knowledgeTopicRepository;
    private final KnowledgeRevisionHistoryRepository historyRepository;
    private final KnowledgeDecayEngine knowledgeDecayEngine;

    public KnowledgeService(
            KnowledgeTopicRepository knowledgeTopicRepository,
            KnowledgeRevisionHistoryRepository historyRepository,
            KnowledgeDecayEngine knowledgeDecayEngine
    ) {
        this.knowledgeTopicRepository = knowledgeTopicRepository;
        this.historyRepository = historyRepository;
        this.knowledgeDecayEngine = knowledgeDecayEngine;
    }

    /*
     * Create a new knowledge topic
     */
    public KnowledgeTopicResponse createTopic(User owner, CreateKnowledgeTopicRequest request) {
        KnowledgeTopic topic = new KnowledgeTopic();
        topic.setOwner(owner);
        topic.setTitle(request.title() != null ? request.title().trim() : null);
        topic.setSubject(request.subject() != null ? request.subject().trim() : null);
        topic.setContent(request.content() != null ? request.content().trim() : null);
        topic.setCreatedAt(topic.getCreatedAt() != null ? topic.getCreatedAt() : LocalDateTime.now());
        topic.setLastReviewed(topic.getLastReviewed() != null ? topic.getLastReviewed() : LocalDate.now());
        topic.setLastDecayCheck(topic.getLastDecayCheck() != null ? topic.getLastDecayCheck() : topic.getLastReviewed());
        topic.setStatus(topic.getStatus() != null ? topic.getStatus() : KnowledgeStatus.CURRENT);
        topic.setMasteryLevel(topic.getMasteryLevel() > 0 ? topic.getMasteryLevel() : 100.0);
        topic.setDecayRate(topic.getDecayRate() > 0 ? topic.getDecayRate() : 0.1);
        topic.ensureKnowledgeDefaults();
        knowledgeDecayEngine.applyDecay(topic, LocalDate.now());

        return toResponse(knowledgeTopicRepository.save(topic));
    }

    /*
     * Review a topic and restore mastery on the 0-100 scale.
     */
    public KnowledgeTopicResponse reviewTopic(UUID topicId) {
        return reviewTopic(topicId, (ReviewKnowledgeTopicRequest) null);
    }

    @Transactional
    public KnowledgeTopicResponse reviewTopic(UUID topicId, String newContent) {
        return reviewTopic(topicId, new ReviewKnowledgeTopicRequest(newContent, null, null, null));
    }

    @Transactional
    public KnowledgeTopicResponse reviewTopic(UUID topicId, ReviewKnowledgeTopicRequest request) {
        KnowledgeTopic topic = getTopicOrThrow(topicId);
        topic.ensureKnowledgeDefaults();
        String oldContent = topic.getContent();

        String updatedContent = request != null && request.updatedContent() != null && !request.updatedContent().isBlank()
                ? request.updatedContent().trim()
                : null;
        if (updatedContent != null) {
            topic.setContent(updatedContent);
            topic.setDecayRate(Math.max(0.03, topic.getDecayRate() * 0.93));
        }

        KnowledgeDecayEngine.ReviewOutcome reviewOutcome = knowledgeDecayEngine.processReview(topic, request, LocalDate.now());

        KnowledgeRevisionHistory history = new KnowledgeRevisionHistory();
        history.setOldContent(oldContent);
        history.setNewContent(updatedContent != null ? updatedContent : topic.getContent());
        history.setEditedAt(LocalDateTime.now());
        history.setPreviousMasteryLevel(reviewOutcome.masteryBeforeReview());
        history.setNewMasteryLevel(topic.getMasteryLevel());
        history.setPreviousStabilityDays(reviewOutcome.stabilityBeforeReview());
        history.setNewStabilityDays(topic.getStabilityDays());
        history.setRecallScore(reviewOutcome.effectiveRecallScore());
        history.setConfidenceScore(request != null ? request.confidenceScore() : null);
        history.setResponseTimeSeconds(request != null ? request.responseTimeSeconds() : null);
        history.setTopic(topic);
        historyRepository.save(history);

        return toResponse(knowledgeTopicRepository.save(topic));
    }

    /*
     * Apply forgetting curve decay to a single topic.
     */
    public KnowledgeTopicResponse applyDecay(UUID topicId) {
        KnowledgeTopic topic = getTopicOrThrow(topicId);
        applyDecayState(topic);

        return toResponse(knowledgeTopicRepository.save(topic));
    }

    /*
     * Apply decay to all topics in the system.
     */
    public void applyDecayToAllTopics() {
        List<KnowledgeTopic> topics = knowledgeTopicRepository.findAll();

        for (KnowledgeTopic topic : topics) {
            if (topic.getStatus() == KnowledgeStatus.DRAFT) {
                continue;
            }

            applyDecayState(topic);
        }

        knowledgeTopicRepository.saveAll(topics);
    }

    /*
     * Get topics needing revision
     */
    public List<KnowledgeTopicResponse> getTopicsNeedingRevision(User owner) {
        List<KnowledgeTopic> topics = knowledgeTopicRepository.findByOwner_IdOrderByCreatedAtDesc(owner.getId());
        refreshTopicStates(topics);

        return topics.stream()
                .filter(topic -> topic.getStatus() == KnowledgeStatus.NEEDS_REVISION)
                .map(this::toResponse)
                .toList();
    }

    /*
     * Get topics for a specific user
     */
    public List<KnowledgeTopicResponse> getTopicsForUser(User owner) {
        List<KnowledgeTopic> topics = knowledgeTopicRepository.findByOwner_IdOrderByCreatedAtDesc(owner.getId());
        refreshTopicStates(topics);

        return topics
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<KnowledgeTopicResponse> getTopicsByUser(UUID userId) {
        List<KnowledgeTopic> topics = knowledgeTopicRepository.findByOwner_IdOrderByCreatedAtDesc(userId);
        refreshTopicStates(topics);

        return topics
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private KnowledgeTopic getTopicOrThrow(UUID topicId) {
        return knowledgeTopicRepository
                .findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
    }

    private void applyDecayState(KnowledgeTopic topic) {
        if (topic.getStatus() == KnowledgeStatus.DRAFT) {
            return;
        }

        knowledgeDecayEngine.applyDecay(topic, LocalDate.now());
    }

    private void refreshTopicStates(List<KnowledgeTopic> topics) {
        if (topics.isEmpty()) {
            return;
        }

        topics.forEach(this::applyDecayState);
        knowledgeTopicRepository.saveAll(topics);
    }

    public KnowledgeTopicResponse toResponse(KnowledgeTopic topic) {
        return new KnowledgeTopicResponse(
                topic.getTopicId(),
                topic.getTitle(),
                topic.getSubject(),
                topic.getContent(),
                topic.getStatus() != null ? topic.getStatus().name() : null,
                topic.getMasteryLevel(),
                topic.getRetrievabilityScore(),
                topic.getDecayRate(),
                topic.getStabilityDays(),
                topic.getEaseFactor(),
                topic.getDifficultyIndex(),
                topic.getNextReviewDate(),
                topic.getReviewCount(),
                topic.getSuccessfulReviews(),
                topic.getConsecutiveSuccessfulReviews(),
                topic.getLapseCount(),
                topic.getLastRecallScore(),
                topic.getAverageRecallScore(),
                topic.getLastReviewed(),
                topic.getLastDecayCheck(),
                topic.getCreatedAt(),
                topic.getStatus() == KnowledgeStatus.NEEDS_REVISION
        );
    }
}
