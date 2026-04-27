package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.model.TimeCapsuleKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TimeCapsuleKnowledgeRepository extends JpaRepository<TimeCapsuleKnowledge, Long> {

    List<TimeCapsuleKnowledge> findBySnapshot_IdOrderByTopicNameAsc(Long snapshotId);

    List<TimeCapsuleKnowledge> findBySnapshot_IdIn(Collection<Long> snapshotIds);
}
