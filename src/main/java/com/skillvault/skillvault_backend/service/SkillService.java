package com.skillvault.skillvault_backend.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillvault.skillvault_backend.enums.SkillLevel;
import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.repository.SkillRepository;

@Service
public class SkillService {

    private final SkillRepository repository;

    public SkillService(SkillRepository repository) {
        this.repository = repository;
    }

    public Skill createSkill(Skill skill) {

        skill.setIsActive(true);
        skill.setSkillScore(0.0);
        skill.setConfidenceIndex(100.0);
        skill.setUsageFrequency(0);
        skill.setLastUsed(LocalDate.now());

        return repository.save(skill);
    }

    // Evolution through practice
    public Skill evaluateSkill(UUID skillId, double hours) {

        Skill skill = repository.findById(skillId).orElseThrow();

        double growthFactor = 1.5;

        skill.setSkillScore(skill.getSkillScore() + hours * growthFactor);

        skill.setUsageFrequency(skill.getUsageFrequency() + 1);

        skill.setLastUsed(LocalDate.now());

        return repository.save(skill);
    }

    // Skill entropy / decay
    public Skill applyDecay(UUID skillId) {

        Skill skill = repository.findById(skillId).orElseThrow();

        long days = ChronoUnit.DAYS.between(skill.getLastUsed(), LocalDate.now());

        double newConfidence =
                skill.getConfidenceIndex() - (days * 0.5);

        if(newConfidence < 0) newConfidence = 0;

        skill.setConfidenceIndex(newConfidence);

        return repository.save(skill);
    }

    public SkillLevel getSkillLevel(Skill skill){

        double score = skill.getSkillScore();

        if(score < 30) return SkillLevel.BEGINNER;

        if(score < 70) return SkillLevel.INTERMEDIATE;

        return SkillLevel.ADVANCED;
    }

    public List<Skill> getUserSkills(UUID userId){
        return repository.findByUser(userId);
    }

}