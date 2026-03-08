package com.skillvault.skillvault_backend.model;

import com.skillvault.skillvault_backend.enums.*;
import jakarta.persistence.*;
import lombok.*;


import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    private Integer creditBalance;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private Double reputationScore;

    private Integer tradesCompleted;

    private Integer noShowCount;
}
