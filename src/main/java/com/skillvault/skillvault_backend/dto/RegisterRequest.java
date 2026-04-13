package com.skillvault.skillvault_backend.dto;

public record RegisterRequest(
        String name,
        String email,
        String password,
        com.skillvault.skillvault_backend.enums.UserRole role
) {
}
