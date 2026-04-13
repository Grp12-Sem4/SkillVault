package com.skillvault.skillvault_backend.controller;

import com.skillvault.skillvault_backend.model.Skill;
import com.skillvault.skillvault_backend.model.User;
import com.skillvault.skillvault_backend.repository.UserRepository;
import com.skillvault.skillvault_backend.service.SkillService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService service;
    private final UserRepository userRepository;

    public SkillController(SkillService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Skill createSkill(@RequestBody Skill skill, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        User authenticatedUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        skill.setUser(authenticatedUser);
        return service.createSkill(skill);
    }

    @PutMapping("/{skillId}/practice")
    public Skill practiceSkill(@PathVariable UUID skillId, @RequestParam double hours) {
        return service.evaluateSkill(skillId, hours);
    }

    @PutMapping("/{skillId}/decay")
    public Skill applyDecay(@PathVariable UUID skillId) {
        return service.applyDecay(skillId);
    }

    @GetMapping("/my")
    public List<Skill> getUserSkills(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        return service.getUserSkills(user);
    }
}
