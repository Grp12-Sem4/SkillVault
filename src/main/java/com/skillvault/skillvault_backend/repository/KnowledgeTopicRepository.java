package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.model.KnowledgeTopic;
import com.skillvault.skillvault_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KnowledgeTopicRepository extends JpaRepository<KnowledgeTopic, UUID> {

    List<KnowledgeTopic> findByAuthor(User author);

}