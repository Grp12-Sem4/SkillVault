package com.skillvault.skillvault_backend.model;

import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "knowledge_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeTopic {

    @Id
    @GeneratedValue
    @Column(name = "topic_id")
    private UUID topicId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    private String title;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private KnowledgeStatus status;

    @Column(name = "mastery_level")
    private double masteryLevel = 100.0;

    @Column(name = "decay_rate")
    private double decayRate = 0.1;

    @Column(name = "stability_days")
    private double stabilityDays = 1.0;

    @Column(name = "ease_factor")
    private double easeFactor = 2.5;

    @Column(name = "retrievability_score")
    private double retrievabilityScore = 100.0;

    @Column(name = "difficulty_index")
    private double difficultyIndex = 1.4;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate = LocalDate.now().plusDays(1);

    @Column(name = "review_count")
    private int reviewCount = 0;

    @Column(name = "successful_reviews")
    private int successfulReviews = 0;

    @Column(name = "consecutive_successful_reviews")
    private int consecutiveSuccessfulReviews = 0;

    @Column(name = "lapse_count")
    private int lapseCount = 0;

    @Column(name = "last_recall_score")
    private int lastRecallScore = 5;

    @Column(name = "average_recall_score")
    private double averageRecallScore = 5.0;

    @Column(name = "last_reviewed")
    private LocalDate lastReviewed = LocalDate.now();

    @Column(name = "last_decay_check")
    private LocalDate lastDecayCheck = LocalDate.now();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Calculates current mastery using the elapsed time since the last
     * review or decay checkpoint, whichever is newer.
     */
    public double calculateCurrentMastery() {
        return calculateCurrentMastery(LocalDate.now());
    }

    public double calculateCurrentMastery(LocalDate asOf) {
        LocalDate baselineDate = this.lastDecayCheck != null ? this.lastDecayCheck : this.lastReviewed;
        if (baselineDate == null) {
            return Math.max(0.0, Math.min(100.0, this.masteryLevel));
        }

        long daysPassed = ChronoUnit.DAYS.between(baselineDate, asOf);
        if (daysPassed <= 0) {
            return Math.max(0.0, Math.min(100.0, this.masteryLevel));
        }

        double effectiveStability = Math.max(0.5, this.stabilityDays);
        double currentMastery = this.masteryLevel * Math.exp(-(this.decayRate * daysPassed) / effectiveStability);
        return Math.max(0.0, Math.min(100.0, currentMastery));
    }

    public double calculateRetrievability(LocalDate asOf) {
        if (this.lastReviewed == null) {
            return Math.max(0.0, Math.min(100.0, this.retrievabilityScore));
        }

        long daysSinceReview = ChronoUnit.DAYS.between(this.lastReviewed, asOf);
        if (daysSinceReview <= 0) {
            return Math.max(0.0, Math.min(100.0, this.masteryLevel));
        }

        double effectiveStability = Math.max(0.5, this.stabilityDays);
        double theoreticalRecall = 100.0 * Math.exp(-(this.decayRate * daysSinceReview) / effectiveStability);
        double boundedRecall = Math.min(this.calculateCurrentMastery(asOf), theoreticalRecall);
        return Math.max(0.0, Math.min(100.0, boundedRecall));
    }

    public boolean isReviewDue(LocalDate asOf) {
        return this.nextReviewDate != null && !this.nextReviewDate.isAfter(asOf);
    }

    public void ensureKnowledgeDefaults() {
        this.masteryLevel = clamp(this.masteryLevel > 0 ? this.masteryLevel : 100.0);
        this.decayRate = Math.max(0.03, this.decayRate > 0 ? this.decayRate : 0.1);
        this.stabilityDays = Math.max(1.0, this.stabilityDays > 0 ? this.stabilityDays : 1.0);
        this.easeFactor = clamp(this.easeFactor > 0 ? this.easeFactor : 2.5, 1.3, 3.2);
        this.difficultyIndex = clamp(this.difficultyIndex > 0 ? this.difficultyIndex : 1.4, 1.0, 5.0);
        this.averageRecallScore = clamp(this.averageRecallScore > 0 ? this.averageRecallScore : 5.0, 0.0, 5.0);
        this.lastRecallScore = (int) clamp(this.lastRecallScore > 0 ? this.lastRecallScore : 5, 0.0, 5.0);
        this.lastReviewed = this.lastReviewed != null ? this.lastReviewed : LocalDate.now();
        this.lastDecayCheck = this.lastDecayCheck != null ? this.lastDecayCheck : this.lastReviewed;
        this.createdAt = this.createdAt != null ? this.createdAt : LocalDateTime.now();
        this.nextReviewDate = this.nextReviewDate != null ? this.nextReviewDate : this.lastReviewed.plusDays(1);
        this.retrievabilityScore = clamp(this.retrievabilityScore > 0 ? this.retrievabilityScore : this.masteryLevel);
        this.status = this.status != null ? this.status : KnowledgeStatus.CURRENT;
    }

    private double clamp(double value) {
        return clamp(value, 0.0, 100.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
