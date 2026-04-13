package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import com.skillvault.skillvault_backend.model.KnowledgeRevisionHistory;
import com.skillvault.skillvault_backend.model.KnowledgeTopic;
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

    public KnowledgeService(
            KnowledgeTopicRepository knowledgeTopicRepository,
            KnowledgeRevisionHistoryRepository historyRepository
    ) {
        this.knowledgeTopicRepository = knowledgeTopicRepository;
        this.historyRepository = historyRepository;
    }

    /*
     * Create a new knowledge topic
     */
    public KnowledgeTopic createTopic(KnowledgeTopic topic) {
        topic.setCreatedAt(topic.getCreatedAt() != null ? topic.getCreatedAt() : LocalDateTime.now());
        topic.setLastReviewed(topic.getLastReviewed() != null ? topic.getLastReviewed() : LocalDate.now());
        topic.setLastDecayCheck(topic.getLastDecayCheck() != null ? topic.getLastDecayCheck() : topic.getLastReviewed());
        topic.setStatus(topic.getStatus() != null ? topic.getStatus() : KnowledgeStatus.CURRENT);
        topic.setMasteryLevel(topic.getMasteryLevel() > 0 ? topic.getMasteryLevel() : 100.0);
        topic.setDecayRate(topic.getDecayRate() > 0 ? topic.getDecayRate() : 0.1);

        return knowledgeTopicRepository.save(topic);
    }

    /*
     * Review a topic and restore mastery on the 0-100 scale.
     */
    public KnowledgeTopic reviewTopic(UUID topicId) {
        KnowledgeTopic topic = getTopicOrThrow(topicId);

        topic.setMasteryLevel(100.0);
        topic.setLastReviewed(LocalDate.now());
        topic.setLastDecayCheck(LocalDate.now());
        topic.setStatus(KnowledgeStatus.CURRENT);

        return knowledgeTopicRepository.save(topic);
    }

    @Transactional
    public KnowledgeTopic reviewTopic(UUID topicId, String newContent) {
        KnowledgeTopic topic = getTopicOrThrow(topicId);

        KnowledgeRevisionHistory history = new KnowledgeRevisionHistory();
        history.setOldContent(topic.getContent());
        history.setNewContent(newContent);
        history.setEditedAt(LocalDateTime.now());
        history.setTopic(topic);
        historyRepository.save(history);

        topic.setDecayRate(Math.max(0.01, topic.getDecayRate() * 0.85));
        topic.setContent(newContent);
        topic.setMasteryLevel(100.0);
        topic.setLastReviewed(LocalDate.now());
        topic.setLastDecayCheck(LocalDate.now());
        topic.setStatus(KnowledgeStatus.CURRENT);

        return knowledgeTopicRepository.save(topic);
    }

    /*
     * Apply forgetting curve decay to a single topic.
     */
    public KnowledgeTopic applyDecay(UUID topicId) {
        KnowledgeTopic topic = getTopicOrThrow(topicId);
        applyDecayState(topic);

        return knowledgeTopicRepository.save(topic);
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
    public List<KnowledgeTopic> getTopicsNeedingRevision() {
        return knowledgeTopicRepository.findByStatus(KnowledgeStatus.NEEDS_REVISION);
    }

    /*
     * Get topics for a specific user
     */
    public List<KnowledgeTopic> getTopicsByUser(UUID userId) {
        return knowledgeTopicRepository.findByOwnerId(userId);
    }

    private KnowledgeTopic getTopicOrThrow(UUID topicId) {
        return knowledgeTopicRepository
                .findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
    }

    private void applyDecayState(KnowledgeTopic topic) {
        double newMastery = topic.calculateCurrentMastery();
        topic.setMasteryLevel(newMastery);
        topic.setLastDecayCheck(LocalDate.now());

        if (newMastery < 40.0) {
            topic.setStatus(KnowledgeStatus.NEEDS_REVISION);
        } else if (newMastery < 75.0) {
            topic.setStatus(KnowledgeStatus.DECAYING);
        } else {
            topic.setStatus(KnowledgeStatus.CURRENT);
        }
    }
}
