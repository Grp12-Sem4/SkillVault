package com.skillvault.skillvault_backend.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.skillvault.skillvault_backend.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "skills")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "skill_category", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "skillCategory")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TechnicalSkill.class, name = "TECHNICAL"),
        @JsonSubTypes.Type(value = SoftSkill.class, name = "SOFT")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Skill {
    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private SkillType type;

    private Integer creditValue;

    private String category;

    private Boolean isActive;

    private Double skillScore = 0.0;

    private Double confidenceIndex = 100.0;

    private Integer usageFrequency = 0;

    private LocalDate lastUsed;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public abstract void evaluate(double input);
}
