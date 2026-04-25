package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.dto.ReviewKnowledgeTopicRequest;
import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeDecayEngineTest {

    private final KnowledgeDecayEngine knowledgeDecayEngine = new KnowledgeDecayEngine();

    @Test
    void applyDecayShouldReduceMasteryAndFlagOverdueTopic() {
        KnowledgeTopic topic = new KnowledgeTopic();
        topic.setTitle("Operating Systems");
        topic.setMasteryLevel(100.0);
        topic.setDecayRate(0.2);
        topic.setStabilityDays(2.0);
        topic.setLastReviewed(LocalDate.now().minusDays(14));
        topic.setLastDecayCheck(LocalDate.now().minusDays(14));
        topic.setNextReviewDate(LocalDate.now().minusDays(10));
        topic.ensureKnowledgeDefaults();

        knowledgeDecayEngine.applyDecay(topic, LocalDate.now());

        assertTrue(topic.getMasteryLevel() < 40.0);
        assertTrue(topic.getRetrievabilityScore() < 40.0);
        assertEquals(KnowledgeStatus.NEEDS_REVISION, topic.getStatus());
    }

    @Test
    void strongRecallShouldIncreaseStabilityAndScheduleLaterReview() {
        KnowledgeTopic topic = new KnowledgeTopic();
        topic.setTitle("Data Structures");
        topic.setMasteryLevel(78.0);
        topic.setDecayRate(0.1);
        topic.setStabilityDays(6.0);
        topic.setEaseFactor(2.5);
        topic.setReviewCount(2);
        topic.setSuccessfulReviews(2);
        topic.setConsecutiveSuccessfulReviews(2);
        topic.setAverageRecallScore(4.0);
        topic.setLastReviewed(LocalDate.now().minusDays(4));
        topic.setLastDecayCheck(LocalDate.now().minusDays(4));
        topic.setNextReviewDate(LocalDate.now());
        topic.ensureKnowledgeDefaults();

        knowledgeDecayEngine.processReview(
                topic,
                new ReviewKnowledgeTopicRequest("Refined notes", 5, 95, 12),
                LocalDate.now()
        );

        assertTrue(topic.getStabilityDays() > 6.0);
        assertTrue(topic.getMasteryLevel() >= 95.0);
        assertTrue(topic.getNextReviewDate().isAfter(LocalDate.now().plusDays(5)));
        assertEquals(KnowledgeStatus.CURRENT, topic.getStatus());
    }

    @Test
    void failedRecallShouldCreateLapseAndShorterReviewCycle() {
        KnowledgeTopic topic = new KnowledgeTopic();
        topic.setTitle("Compiler Design");
        topic.setMasteryLevel(62.0);
        topic.setDecayRate(0.12);
        topic.setStabilityDays(10.0);
        topic.setEaseFactor(2.6);
        topic.setReviewCount(4);
        topic.setSuccessfulReviews(3);
        topic.setConsecutiveSuccessfulReviews(3);
        topic.setAverageRecallScore(4.2);
        topic.setLastReviewed(LocalDate.now().minusDays(11));
        topic.setLastDecayCheck(LocalDate.now().minusDays(11));
        topic.setNextReviewDate(LocalDate.now().minusDays(1));
        topic.ensureKnowledgeDefaults();

        knowledgeDecayEngine.processReview(
                topic,
                new ReviewKnowledgeTopicRequest(null, 1, 25, 220),
                LocalDate.now()
        );

        assertTrue(topic.getLastRecallScore() <= 1);
        assertEquals(0, topic.getConsecutiveSuccessfulReviews());
        assertEquals(1, topic.getLapseCount());
        assertTrue(topic.getStabilityDays() < 10.0);
        assertTrue(!topic.getNextReviewDate().isAfter(LocalDate.now().plusDays(5)));
    }
}
