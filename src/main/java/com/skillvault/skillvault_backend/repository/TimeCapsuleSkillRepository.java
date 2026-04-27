package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.model.TimeCapsuleSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TimeCapsuleSkillRepository extends JpaRepository<TimeCapsuleSkill, Long> {

    List<TimeCapsuleSkill> findBySnapshot_IdOrderBySkillNameAsc(Long snapshotId);

    List<TimeCapsuleSkill> findBySnapshot_IdIn(Collection<Long> snapshotIds);
}
