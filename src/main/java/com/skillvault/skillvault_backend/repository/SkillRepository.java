package com.skillvault.skillvault_backend.repository;

import com.skillvault.skillvault_backend.enums.SkillType;
import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.model.SoftSkill;
import com.skillvault.skillvault_backend.model.TechnicalSkill;
import com.skillvault.skillvault_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    List<Skill> findByUser(User user);

    List<Skill> findByTypeAndUser_IdNot(SkillType type, UUID userId);

    Optional<Skill> findByUserAndTitle(User user, String title);

    long countByUser_Id(UUID userId);

    long countByUser_IdAndType(UUID userId, SkillType type);

    @Query("select count(s) from Skill s where s.user.id = :userId and type(s) = TechnicalSkill")
    long countTechnicalSkillsByUserId(@Param("userId") UUID userId);

    @Query("select count(s) from Skill s where s.user.id = :userId and type(s) = SoftSkill")
    long countSoftSkillsByUserId(@Param("userId") UUID userId);
}