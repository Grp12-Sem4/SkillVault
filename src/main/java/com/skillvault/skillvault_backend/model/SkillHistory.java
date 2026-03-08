package com.skillvault.skillvault_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="skill_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkillHistory {

    @Id
    @GeneratedValue
    private UUID id;

    private String previousDescription;

    private String newDescription;

    private LocalDateTime modifiedAt;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;

}
