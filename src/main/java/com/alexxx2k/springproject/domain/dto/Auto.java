package com.alexxx2k.springproject.domain.dto;

public record Auto(
        Integer id,
        String num,
        String color,
        String mark,
        Integer personalId
) {}