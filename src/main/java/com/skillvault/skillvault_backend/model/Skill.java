package com.skillvault.skillvault_backend.model;

import com.skillvault.skillvault_backend.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;
import java.time.LocalDate;
@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Skill {
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


}
