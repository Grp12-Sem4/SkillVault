package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.model.SkillHistory;
import com.skillvault.skillvault_backend.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface SkillHistoryRepository extends JpaRepository<SkillHistory, UUID> {

    List<SkillHistory> findBySkill(Skill skill);

}
