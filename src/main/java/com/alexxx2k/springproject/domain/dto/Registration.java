package com.alexxx2k.springproject.domain.dto;

public record Registration(
        String name,
        String email,
        String password,
        String confirmPassword,
        String cityName
) {}
