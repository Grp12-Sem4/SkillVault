package com.skillvault.skillvault_backend.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import com.skillvault.skillvault_backend.repository.KnowledgeTopicRepository;

@Component
public class DecayEngine {
    private final KnowledgeTopicRepository repository;

    public DecayEngine(KnowledgeTopicRepository repository) {
        this.repository = repository;
    }

    // Runs every midnight. For testing, use (fixedRate = 10000) for 10 seconds.
    @Scheduled(cron = "0 0 0 * * *") 
    public void runDecayScan() {
        List<KnowledgeTopic> topics = repository.findAll();
        for (KnowledgeTopic topic : topics) {
            if (topic.getStatus() == KnowledgeStatus.DRAFT) continue;

            double currentMastery = topic.calculateCurrentMastery();
            topic.setMasteryLevel(currentMastery);

            // State Transitions
            if (currentMastery < 40.0) topic.setStatus(KnowledgeStatus.NEEDS_REVISION);
            else if (currentMastery < 75.0) topic.setStatus(KnowledgeStatus.DECAYING);

            repository.save(topic);
        }
        System.out.println("System Entropy Check: Knowledge Decay Updated.");
    }
}