package com.skillvault.skillvault_backend.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.repository.SkillRepository;

@Component
public class SkillEvolutionEngine {

    private final SkillRepository repository;

    public SkillEvolutionEngine(SkillRepository repository){
        this.repository = repository;
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void runSkillEntropy(){

        List<Skill> skills = repository.findAll();

        for(Skill skill : skills){

            if(skill.getConfidenceIndex() < 40){
                skill.setIsActive(false);
            }

            repository.save(skill);
        }

        System.out.println("Skill entropy system updated.");
    }
}