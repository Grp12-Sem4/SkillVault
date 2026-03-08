package com.skillvault.skillvault_backend.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.skillvault.skillvault_backend.enums.KnowledgeStatus;

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
@Table(name="knowledge_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeTopic {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private KnowledgeStatus status;

    // --- DECAY ENGINE FIELDS ---
    private double masteryLevel = 100.0;     // Percentage 0-100
    private double decayRate = 0.1;          // The "k" factor in the curve
    private LocalDateTime lastReviewedAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name="author_id")
    private User author;

    /**
     * Calculates mastery based on entropy: Mastery = initial * e^(-k * days)
     */
    public double calculateCurrentMastery() {
        long daysPassed = ChronoUnit.DAYS.between(this.lastReviewedAt, LocalDateTime.now());
        if (daysPassed <= 0) return this.masteryLevel;

        // Exponential Model implementation
        double currentMastery = this.masteryLevel * Math.exp(-this.decayRate * daysPassed);
        return Math.max(0.0, Math.min(100.0, currentMastery));
    }
}