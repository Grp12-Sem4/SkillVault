package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import com.skillvault.skillvault_backend.repository.KnowledgeTopicRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeService {

    private final KnowledgeTopicRepository knowledgeTopicRepository;

    public KnowledgeService(KnowledgeTopicRepository knowledgeTopicRepository) {
        this.knowledgeTopicRepository = knowledgeTopicRepository;
    }

    /**
     * Create a new knowledge topic
     */
    public KnowledgeTopic createTopic(KnowledgeTopic topic) {

        topic.setCreatedAt(java.time.LocalDateTime.now());
        topic.setLastReviewed(LocalDate.now());
        topic.setStatus(KnowledgeStatus.CURRENT);

        return knowledgeTopicRepository.save(topic);
    }

    /**
     * Review a topic (resets mastery)
     */
    public KnowledgeTopic reviewTopic(UUID topicId) {

        KnowledgeTopic topic = knowledgeTopicRepository
                .findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        topic.setMasteryLevel(10.0);
        topic.setLastReviewed(LocalDate.now());
        topic.setStatus(KnowledgeStatus.CURRENT);

        return knowledgeTopicRepository.save(topic);
    }

    /**
     * Apply forgetting curve decay to a single topic
     */
    public KnowledgeTopic applyDecay(UUID topicId) {

        KnowledgeTopic topic = knowledgeTopicRepository
                .findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        long days = ChronoUnit.DAYS.between(
                topic.getLastReviewed(),
                LocalDate.now()
        );

        double newMastery =
                topic.getMasteryLevel() *
                Math.exp(-topic.getDecayRate() * days);

        topic.setMasteryLevel(newMastery);

        if (newMastery < 4) {
            topic.setStatus(KnowledgeStatus.NEEDS_REVISION);
        }

        return knowledgeTopicRepository.save(topic);
    }

    /**
     * Apply decay to all topics in the system
     */
    public void applyDecayToAllTopics() {

        List<KnowledgeTopic> topics = knowledgeTopicRepository.findAll();

        for (KnowledgeTopic topic : topics) {

            long days = ChronoUnit.DAYS.between(
                    topic.getLastReviewed(),
                    LocalDate.now()
            );

            double newMastery =
                    topic.getMasteryLevel() *
                    Math.exp(-topic.getDecayRate() * days);

            topic.setMasteryLevel(newMastery);

            if (newMastery < 4) {
                topic.setStatus(KnowledgeStatus.NEEDS_REVISION);
            }

            knowledgeTopicRepository.save(topic);
        }
    }

    /**
     * Get topics needing revision
     */
    public List<KnowledgeTopic> getTopicsNeedingRevision() {

        return knowledgeTopicRepository.findAll()
                .stream()
                .filter(topic -> topic.getMasteryLevel() < 4)
                .toList();
    }

    /**
     * Get topics for a specific user
     */
    public List<KnowledgeTopic> getTopicsByUser(UUID userId) {
        return knowledgeTopicRepository.findByOwnerId(userId);
    }

}