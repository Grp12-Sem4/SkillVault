package com.skillvault.skillvault_backend.model;

import com.skillvault.skillvault_backend.enums.CreditTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="credit_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransaction {
    @Id
    @GeneratedValue
    private UUID id;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private CreditTransactionType type;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="trade_id")
    private TradeSession trade;

}
