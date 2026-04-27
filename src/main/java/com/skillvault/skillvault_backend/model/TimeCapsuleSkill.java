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
@Table(name = "time_capsule_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeCapsuleSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private TimeCapsuleSnapshot snapshot;

    @Column(name = "source_skill_id")
    private UUID sourceSkillId;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Column(name = "skill_score", nullable = false)
    private Double skillScore;

    @Column(name = "confidence_index", nullable = false)
    private Double confidenceIndex;

    @Column(name = "mastery_level", nullable = false)
    private Integer masteryLevel;

    @Column(name = "practice_count", nullable = false)
    private Integer practiceCount;

    @Column(name = "teaching_count", nullable = false)
    private Integer teachingCount;

    @Column(name = "usage_frequency", nullable = false)
    private Integer usageFrequency;
}
