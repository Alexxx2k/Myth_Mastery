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
) {}