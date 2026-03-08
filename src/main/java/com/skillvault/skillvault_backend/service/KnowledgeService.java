package com.skillvault.skillvault_backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import com.skillvault.skillvault_backend.model.KnowledgeRevisionHistory;
import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import com.skillvault.skillvault_backend.repository.KnowledgeRevisionHistoryRepository;
import com.skillvault.skillvault_backend.repository.KnowledgeTopicRepository;

@Service
public class KnowledgeService {
    private final KnowledgeTopicRepository repository;
    private final KnowledgeRevisionHistoryRepository historyRepository;

    public KnowledgeService(KnowledgeTopicRepository repository, KnowledgeRevisionHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public KnowledgeTopic reviewTopic(UUID topicId, String newContent) {
        KnowledgeTopic topic = repository.findById(topicId).orElseThrow();

        // 1. Log to history
        KnowledgeRevisionHistory history = new KnowledgeRevisionHistory();
        history.setOldContent(topic.getContent());
        history.setNewContent(newContent);
        history.setEditedAt(LocalDateTime.now());
        history.setTopic(topic);
        historyRepository.save(history);

        // 2. Apply Spaced Repetition (Decrease decayRate by 15%)
        topic.setDecayRate(Math.max(0.01, topic.getDecayRate() * 0.85)); 
        
        // 3. Reset mastery
        topic.setContent(newContent);
        topic.setMasteryLevel(100.0);
        topic.setLastReviewedAt(LocalDateTime.now());
        topic.setStatus(KnowledgeStatus.STABLE);

        return repository.save(topic);
    }
}