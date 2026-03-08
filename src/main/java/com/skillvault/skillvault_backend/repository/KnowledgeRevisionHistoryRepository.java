package com.skillvault.skillvault_backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillvault.skillvault_backend.model.KnowledgeRevisionHistory;

public interface KnowledgeRevisionHistoryRepository extends JpaRepository<KnowledgeRevisionHistory, UUID> {
}