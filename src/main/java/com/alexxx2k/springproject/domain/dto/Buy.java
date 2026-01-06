package com.alexxx2k.springproject.domain.dto;

public record Buy(
        Long id,
        Long customerId,
        Long buyStepId,
        String description
) {
    public static Buy forCreation(Long customerId, String description) {
        return new Buy(null, customerId, null, description);
    }
}
