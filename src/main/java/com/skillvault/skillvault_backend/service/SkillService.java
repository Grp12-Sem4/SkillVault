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

        skill.evaluate(hours);
        skill.setUsageFrequency((skill.getUsageFrequency() == null ? 0 : skill.getUsageFrequency()) + 1);
        skill.setLastUsed(LocalDate.now());

        return repository.save(skill);
    }

    public Skill applyDecay(UUID skillId) {
        Skill skill = repository.findById(skillId).orElseThrow();

        LocalDate lastUsed = skill.getLastUsed() == null ? LocalDate.now() : skill.getLastUsed();
        long days = ChronoUnit.DAYS.between(lastUsed, LocalDate.now());
        double confidenceIndex = skill.getConfidenceIndex() == null ? 100.0 : skill.getConfidenceIndex();
        double newConfidence = confidenceIndex - (days * 0.5);

        skill.setConfidenceIndex(Math.max(newConfidence, 0.0));

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
                skill.getConfidenceIndex(),
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
}
