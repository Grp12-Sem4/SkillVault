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
        LocalDate baselineDate = this.lastDecayCheck != null ? this.lastDecayCheck : this.lastReviewed;
        if (baselineDate == null) {
            return Math.max(0.0, Math.min(100.0, this.masteryLevel));
        }

        long daysPassed = ChronoUnit.DAYS.between(baselineDate, LocalDate.now());
        if (daysPassed <= 0) {
            return Math.max(0.0, Math.min(100.0, this.masteryLevel));
        }

        double currentMastery = this.masteryLevel * Math.exp(-this.decayRate * daysPassed);
        return Math.max(0.0, Math.min(100.0, currentMastery));
    }
}
