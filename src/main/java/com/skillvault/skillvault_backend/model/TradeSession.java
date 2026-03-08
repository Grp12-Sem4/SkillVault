package com.skillvault.skillvault_backend.model;

import com.skillvault.skillvault_backend.enums.TradeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="trade_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeSession {
    @Id
    @GeneratedValue
    private UUID id;

    private LocalDateTime scheduledTime;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    private TradeStatus status;

    @ManyToOne
    @JoinColumn(name="skill_id")
    private Skill skill;

    @ManyToOne
    @JoinColumn(name="requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name="provider_id")
    private User provider;
}
