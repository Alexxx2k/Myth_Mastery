package com.alexxx2k.springproject.domain.dto;

public record Category(
        Long id,
        String name,
        String hazard,
        String rarity
) {

    public static Category forCreation(String name, String hazard, String rarity) {
        return new Category(null,
                name != null ? name : "",
                hazard != null ? hazard : "",
                rarity != null ? rarity : "");
    }
}
