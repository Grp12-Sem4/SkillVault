package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.dto.ReviewKnowledgeTopicRequest;
import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class KnowledgeDecayEngine {

    private static final int DEFAULT_RECALL_SCORE = 4;
    private static final int DEFAULT_CONFIDENCE_SCORE = 75;
    private static final int DEFAULT_RESPONSE_TIME_SECONDS = 45;

    public void applyDecay(KnowledgeTopic topic, LocalDate asOf) {
        topic.ensureKnowledgeDefaults();

        double currentMastery = topic.calculateCurrentMastery(asOf);
        double retrievability = topic.calculateRetrievability(asOf);

        topic.setMasteryLevel(round(currentMastery));
        topic.setRetrievabilityScore(round(retrievability));
        topic.setLastDecayCheck(asOf);
        topic.setStatus(resolveStatus(topic, currentMastery, retrievability, asOf));
    }

    public ReviewOutcome processReview(KnowledgeTopic topic, ReviewKnowledgeTopicRequest request, LocalDate asOf) {
        topic.ensureKnowledgeDefaults();
        applyDecay(topic, asOf);

        int effectiveRecallScore = deriveRecallScore(request);
        double masteryBeforeReview = topic.getMasteryLevel();
        double stabilityBeforeReview = topic.getStabilityDays();
        boolean successfulRecall = effectiveRecallScore >= 3;

        topic.setReviewCount(topic.getReviewCount() + 1);
        topic.setLastRecallScore(effectiveRecallScore);
        topic.setAverageRecallScore(round(calculateAverageRecall(topic, effectiveRecallScore)));

        double updatedEaseFactor = updateEaseFactor(topic.getEaseFactor(), effectiveRecallScore);
        topic.setEaseFactor(round(updatedEaseFactor));

        if (successfulRecall) {
            topic.setSuccessfulReviews(topic.getSuccessfulReviews() + 1);
            topic.setConsecutiveSuccessfulReviews(topic.getConsecutiveSuccessfulReviews() + 1);
            topic.setStabilityDays(round(growStability(topic, effectiveRecallScore)));
        } else {
            topic.setLapseCount(topic.getLapseCount() + 1);
            topic.setConsecutiveSuccessfulReviews(0);
            topic.setStabilityDays(round(resetStability(topic)));
        }

        topic.setDifficultyIndex(round(calculateDifficultyIndex(topic)));

        double recoveredMastery = calculateRecoveredMastery(topic, request, masteryBeforeReview, effectiveRecallScore, successfulRecall);
        topic.setMasteryLevel(round(recoveredMastery));
        topic.setRetrievabilityScore(round(recoveredMastery));
        topic.setLastReviewed(asOf);
        topic.setLastDecayCheck(asOf);
        topic.setNextReviewDate(asOf.plusDays(Math.max(1L, Math.round(topic.getStabilityDays()))));
        topic.setStatus(resolveStatus(topic, topic.getMasteryLevel(), topic.getRetrievabilityScore(), asOf));

        return new ReviewOutcome(effectiveRecallScore, masteryBeforeReview, stabilityBeforeReview, successfulRecall);
    }

    private int deriveRecallScore(ReviewKnowledgeTopicRequest request) {
        int recallScore = request != null && request.recallScore() != null
                ? request.recallScore()
                : DEFAULT_RECALL_SCORE;
        int confidenceScore = request != null && request.confidenceScore() != null
                ? request.confidenceScore()
                : DEFAULT_CONFIDENCE_SCORE;
        int responseTimeSeconds = request != null && request.responseTimeSeconds() != null
                ? request.responseTimeSeconds()
                : DEFAULT_RESPONSE_TIME_SECONDS;

        double weightedScore = recallScore;

        if (confidenceScore >= 90) {
            weightedScore += 0.5;
        } else if (confidenceScore >= 75) {
            weightedScore += 0.25;
        } else if (confidenceScore < 40) {
            weightedScore -= 0.75;
        } else if (confidenceScore < 55) {
            weightedScore -= 0.35;
        }

        if (responseTimeSeconds <= 15) {
            weightedScore += 0.5;
        } else if (responseTimeSeconds <= 30) {
            weightedScore += 0.25;
        } else if (responseTimeSeconds >= 180) {
            weightedScore -= 1.0;
        } else if (responseTimeSeconds >= 90) {
            weightedScore -= 0.5;
        }

        return (int) Math.round(clamp(weightedScore, 0.0, 5.0));
    }

    private double calculateAverageRecall(KnowledgeTopic topic, int latestRecallScore) {
        int priorReviewCount = Math.max(0, topic.getReviewCount() - 1);
        double priorTotal = topic.getAverageRecallScore() * priorReviewCount;
        return (priorTotal + latestRecallScore) / Math.max(1, topic.getReviewCount());
    }

    private double updateEaseFactor(double easeFactor, int recallScore) {
        double updated = easeFactor + (0.1 - (5 - recallScore) * (0.08 + (5 - recallScore) * 0.02));
        return clamp(updated, 1.3, 3.2);
    }

    private double growStability(KnowledgeTopic topic, int recallScore) {
        int streakBeforeCurrentReview = Math.max(0, topic.getConsecutiveSuccessfulReviews());

        if (streakBeforeCurrentReview == 0) {
            return 1.0;
        }

        if (streakBeforeCurrentReview == 1) {
            return Math.max(6.0, topic.getStabilityDays() * 1.8);
        }

        double growthMultiplier = topic.getEaseFactor()
                + ((recallScore - 3) * 0.15)
                + (Math.min(streakBeforeCurrentReview, 8) * 0.05);

        return topic.getStabilityDays() * Math.max(1.2, growthMultiplier);
    }

    private double resetStability(KnowledgeTopic topic) {
        return Math.max(1.0, topic.getStabilityDays() * 0.45);
    }

    private double calculateRecoveredMastery(
            KnowledgeTopic topic,
            ReviewKnowledgeTopicRequest request,
            double masteryBeforeReview,
            int recallScore,
            boolean successfulRecall
    ) {
        int confidenceScore = request != null && request.confidenceScore() != null
                ? request.confidenceScore()
                : DEFAULT_CONFIDENCE_SCORE;
        int responseTimeSeconds = request != null && request.responseTimeSeconds() != null
                ? request.responseTimeSeconds()
                : DEFAULT_RESPONSE_TIME_SECONDS;
        boolean contentRefined = request != null && request.updatedContent() != null && !request.updatedContent().isBlank();

        double confidenceAdjustment = (confidenceScore - 50) / 6.0;
        double responseTimePenalty = Math.max(0.0, (responseTimeSeconds - 30) / 20.0);
        double contentBonus = contentRefined ? 5.0 : 0.0;

        if (successfulRecall) {
            double gain = 24.0
                    + (recallScore * 9.0)
                    + confidenceAdjustment
                    - responseTimePenalty
                    + contentBonus
                    + (Math.min(topic.getConsecutiveSuccessfulReviews(), 6) * 2.0);

            return clamp(Math.max(masteryBeforeReview, 45.0) + gain, 0.0, 100.0);
        }

        double recovery = (masteryBeforeReview * 0.55)
                + 18.0
                + (recallScore * 7.0)
                + confidenceAdjustment
                - responseTimePenalty
                + contentBonus;

        return clamp(recovery, 0.0, 72.0);
    }

    private double calculateDifficultyIndex(KnowledgeTopic topic) {
        double easeComponent = 6.0 - topic.getEaseFactor();
        double lapsePenalty = topic.getLapseCount() * 0.08;
        double weakRecallPenalty = Math.max(0.0, 3.5 - topic.getAverageRecallScore()) * 0.2;
        return clamp(easeComponent + lapsePenalty + weakRecallPenalty, 1.0, 5.0);
    }

    private KnowledgeStatus resolveStatus(KnowledgeTopic topic, double mastery, double retrievability, LocalDate asOf) {
        boolean overdue = topic.isReviewDue(asOf);

        if (mastery < 40.0 || retrievability < 45.0 || (overdue && mastery < 55.0)) {
            return KnowledgeStatus.NEEDS_REVISION;
        }

        if (mastery < 75.0 || retrievability < 70.0 || overdue) {
            return KnowledgeStatus.DECAYING;
        }

        return KnowledgeStatus.CURRENT;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record ReviewOutcome(
            int effectiveRecallScore,
            double masteryBeforeReview,
            double stabilityBeforeReview,
            boolean successfulRecall
    ) {
    }
}
