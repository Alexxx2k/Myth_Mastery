package com.alexxx2k.springproject.domain.dto;

public record User(
        Integer id,
        String username,
        String passwordHash,
        String role,
        Boolean enabled,
        java.time.LocalDateTime createdAt
) {}