package com.skillvault.skillvault_backend.model;

import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

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

    @Column(name = "mastery_level")
    private double masteryLevel;

    @Column(name = "decay_rate")
    private double decayRate;

    @Column(name = "last_reviewed")
    private LocalDate lastReviewed;

    @Enumerated(EnumType.STRING)
    private KnowledgeStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public double calculateCurrentMastery() {
        long days = ChronoUnit.DAYS.between(lastReviewed, LocalDate.now());
        return masteryLevel * Math.exp(-decayRate * days);
    }
}