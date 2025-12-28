package com.alexxx2k.springproject.domain.dto;

public record Customer(
        Long id,
        String name,
        String email,
        String password,
        Long cityId,
        String cityName
) {}
