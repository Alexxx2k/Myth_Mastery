package com.alexxx2k.springproject.domain.dto;

public record Category(
        Long id,
        String name,
        String hazard,
        String rarity
) {
    public Category {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
    }

    public static Category forCreation(String name, String hazard, String rarity) {
        return new Category(null, name, hazard, rarity);
    }
}