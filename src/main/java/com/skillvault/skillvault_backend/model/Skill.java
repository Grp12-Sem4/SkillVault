package com.skillvault.skillvault_backend.model;

import com.skillvault.skillvault_backend.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


}
