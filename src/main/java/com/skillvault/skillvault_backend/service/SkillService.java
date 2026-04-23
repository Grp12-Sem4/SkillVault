package com.skillvault.skillvault_backend.service;

import com.skillvault.skillvault_backend.dto.MarketplaceSkillResponse;
import com.skillvault.skillvault_backend.dto.UserSummaryResponse;
import com.skillvault.skillvault_backend.enums.SkillType;
import com.skillvault.skillvault_backend.model.SoftSkill;
import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.model.TechnicalSkill;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class SkillService {

    private final SkillRepository repository;

    public SkillService(SkillRepository repository) {
        this.repository = repository;
    }

    public Skill createSkill(Skill skill) {
        skill.setIsActive(Boolean.TRUE);
        skill.setSkillScore(0.0);
        skill.setConfidenceIndex(100.0);
        skill.setUsageFrequency(0);
        skill.setLastUsed(LocalDate.now());

        return repository.save(skill);
    }

    public Skill evaluateSkill(UUID skillId, double hours) {
        Skill skill = repository.findById(skillId).orElseThrow();
        skill.setPracticeCount(
            (skill.getPracticeCount() != null ? skill.getPracticeCount() : 0) + 1
        );

        skill.setUsageFrequency(
            (skill.getUsageFrequency() != null ? skill.getUsageFrequency() : 0) + 1
        );
        skill.evaluate(hours);
        skill.setUsageFrequency((skill.getUsageFrequency() == null ? 0 : skill.getUsageFrequency()) + 1);
        skill.setLastUsed(LocalDate.now());
        skill.setLastUsedAt(java.time.LocalDateTime.now());
        skill.setConfidenceIndex(calculateConfidence(skill));
        return repository.save(skill);
    }

    public Skill applyDecay(UUID skillId) {
        Skill skill = repository.findById(skillId).orElseThrow();

        LocalDate lastUsed = skill.getLastUsed() == null ? LocalDate.now() : skill.getLastUsed();
        long days = ChronoUnit.DAYS.between(lastUsed, LocalDate.now());
        double confidenceIndex = skill.getConfidenceIndex() == null ? 100.0 : skill.getConfidenceIndex();
        double newConfidence = confidenceIndex - (days * 0.5);
        skill.setConfidenceIndex(calculateConfidence(skill));
        skill.setLastUsedAt(java.time.LocalDateTime.now());
        return repository.save(skill);
    }

    public List<Skill> getUserSkills(User user) {
        return repository.findByUser(user);
    }

    public List<MarketplaceSkillResponse> getMarketplaceSkills(User currentUser) {
        return repository.findByTypeAndUser_IdNot(SkillType.OFFERED, currentUser.getId()).stream()
                .map(this::toMarketplaceSkillResponse)
                .toList();
    }

    public MarketplaceSkillResponse toMarketplaceSkillResponse(Skill skill) {
        return new MarketplaceSkillResponse(
                skill.getId(),
                skill.getTitle(),
                skill.getDescription(),
                skill.getCategory(),
                skill.getCreditValue(),
                skill.getType(),
                getSkillCategory(skill),
                skill.getSkillScore(),
                skill.getAverageRating(),      
                skill.getRatingCount(),        
                skill.getConfidenceIndex(),   
                skill.getMasteryLevel(),  
                skill.getDifficultyLevel(),
                toUserSummary(skill.getUser())
        );
    }

    public UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreditBalance()
        );
    }

    private String getSkillCategory(Skill skill) {
        if (skill instanceof TechnicalSkill) {
            return "TECHNICAL";
        }

        if (skill instanceof SoftSkill) {
            return "SOFT";
        }

        return null;
    }
    public double calculateConfidence(Skill skill) 
    {

        //double usage = skill.getUsageFrequency() != null ? skill.getUsageFrequency() : 0;
        //double usageScore = usage * 2;
        double practice = skill.getPracticeCount() != null ? skill.getPracticeCount() : 0;
        double teaching = skill.getTeachingCount() != null ? skill.getTeachingCount() : 0;
        double usageScore = (practice * 1.5) + (teaching * 2);
        double avgRating = skill.getAverageRating() != null ? skill.getAverageRating() : 0;
        double ratingScore = avgRating * 20; // scale to 100

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long days = 0;

        if (skill.getLastUsedAt() != null) {
            days = java.time.Duration.between(skill.getLastUsedAt(), now).toDays();
        }

        double recencyScore = Math.max(0, 100 - (days * 2));

        double confidence = (usageScore + ratingScore + recencyScore) / 3;

        return Math.min(100, confidence);
    }

    public String getSkillLevel(Skill skill) {
        int mastery = skill.getMasteryLevel() != null ? skill.getMasteryLevel() : 0;

        if (mastery <= 30) return "BEGINNER";
        if (mastery <= 70) return "INTERMEDIATE";
        return "EXPERT";
    }
}
