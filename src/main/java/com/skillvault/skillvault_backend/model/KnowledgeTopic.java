package com.skillvault.skillvault_backend.model;

import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="knowledge_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeTopic {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private KnowledgeStatus status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name="author_id")
    private User author;
}
