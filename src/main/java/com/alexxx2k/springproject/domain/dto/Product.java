package com.alexxx2k.springproject.domain.dto;

import java.math.BigDecimal;

public record Product(
        Long id,
        Long categoryId,
        String categoryName,
        Long mythologyId,
        String mythologyName,
        String name,
        BigDecimal price,
        String description,
        String pic
) {
    public Product {
        if (name == null) {
            name = "";
        }
    }

    public static Product forCreation(Long categoryId, Long mythologyId,
                                      String name, BigDecimal price,
                                      String description, String pic) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        if (mythologyId == null) {
            throw new IllegalArgumentException("Mythology ID cannot be null");
        }
        return new Product(null, categoryId, null, mythologyId, null,
                name.trim(), price,
                description != null ? description : "",
                pic != null ? pic : "");
    }
}