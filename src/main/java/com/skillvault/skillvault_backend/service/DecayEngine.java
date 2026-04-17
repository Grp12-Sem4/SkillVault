package com.skillvault.skillvault_backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DecayEngine {
    private final KnowledgeService knowledgeService;

    public DecayEngine(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void runDecayScan() {
        knowledgeService.applyDecayToAllTopics();
    }
}
