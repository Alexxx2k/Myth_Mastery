package com.alexxx2k.springproject.domain.dto;

public record ActiveTripsCount(
        Integer routeId,
        String routeName,
        Integer activeTripsCount
) {}