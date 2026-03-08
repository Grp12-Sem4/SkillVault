package com.skillvault.skillvault_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="knowledge_revision_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRevisionHistory {

    @Id
    @GeneratedValue
    private UUID id;

    private String oldContent;

    private String newContent;

    private LocalDateTime editedAt;

    @ManyToOne
    @JoinColumn(name="topic_id")
    private KnowledgeTopic topic;
}
