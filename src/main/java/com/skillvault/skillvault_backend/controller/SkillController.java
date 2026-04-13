package com.skillvault.skillvault_backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.service.SkillService;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService service;

    public SkillController(SkillService service) {
        this.service = service;
    }

    @PostMapping
    public Skill createSkill(@RequestBody Skill skill){
        return service.createSkill(skill);
    }

    @PutMapping("/{skillId}/practice")
    public Skill practiceSkill(
            @PathVariable UUID skillId,
            @RequestParam double hours){

        return service.evaluateSkill(skillId, hours);
    }

    @PutMapping("/{skillId}/decay")
    public Skill applyDecay(@PathVariable UUID skillId){
        return service.applyDecay(skillId);
    }

    @GetMapping("/user/{userId}")
    public List<Skill> getUserSkills(@PathVariable UUID userId){
        return service.getUserSkills(userId);
    }

}