package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.enums.KnowledgeStatus;
import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KnowledgeTopicRepository extends JpaRepository<KnowledgeTopic, UUID> {
    List<KnowledgeTopic> findByStatus(KnowledgeStatus status);

    List<KnowledgeTopic> findByOwnerId(UUID userId);

    List<KnowledgeTopic> findByOwner_IdOrderByCreatedAtDesc(UUID userId);

    List<KnowledgeTopic> findByOwner_IdAndStatusOrderByCreatedAtDesc(UUID userId, KnowledgeStatus status);
}
