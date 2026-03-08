package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    List<Skill> findByUser(User user);

}
