package com.skillvault.skillvault_backend.controller;

import com.skillvault.skillvault_backend.dto.UserProfileResponse;
import com.skillvault.skillvault_backend.service.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return userProfileService.getProfileByEmail(principal.getName());
    }

    @GetMapping("/{userId}")
    public UserProfileResponse getProfileByUserId(@PathVariable UUID userId) {
        return userProfileService.getProfileByUserId(userId);
    }
}
