package com.alexxx2k.springproject.domain.dto;

import java.time.LocalDate;

public record BuyStep(
        Long id,
        Long stepId,
        LocalDate dateStart,
        LocalDate dateEnd
) {}
