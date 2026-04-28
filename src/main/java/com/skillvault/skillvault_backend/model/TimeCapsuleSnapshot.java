package com.skillvault.skillvault_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "time_capsule_snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeCapsuleSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "credit_balance", nullable = false)
    private Integer creditBalance;

    @Column(name = "total_trades", nullable = false)
    private Integer totalTrades;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating;

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TimeCapsuleSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TimeCapsuleKnowledge> knowledgeTopics = new ArrayList<>();

    public void addSkill(TimeCapsuleSkill skill) {
        skills.add(skill);
        skill.setSnapshot(this);
    }

    public void addKnowledgeTopic(TimeCapsuleKnowledge knowledgeTopic) {
        knowledgeTopics.add(knowledgeTopic);
        knowledgeTopic.setSnapshot(this);
    }
}
