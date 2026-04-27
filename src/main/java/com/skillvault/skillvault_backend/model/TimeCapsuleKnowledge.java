package com.skillvault.skillvault_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "time_capsule_knowledge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeCapsuleKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private TimeCapsuleSnapshot snapshot;

    @Column(name = "source_topic_id")
    private UUID sourceTopicId;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "mastery_level", nullable = false)
    private Double masteryLevel;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Column(name = "retrievability_score", nullable = false)
    private Double retrievabilityScore;

    @Column(name = "decay_status", nullable = false)
    private String decayStatus;

    @Column(name = "revision_status", nullable = false)
    private String revisionStatus;
}
