package com.skillvault.skillvault_backend.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SOFT")
@NoArgsConstructor
public class SoftSkill extends Skill {

    @Override
    public void evaluate(double input) {
        double currentScore = getSkillScore() == null ? 0.0 : getSkillScore();
        setSkillScore(currentScore + (input * 1.2));
    }
}
