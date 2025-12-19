package com.alexxx2k.springproject.domain.dto;

import java.time.LocalDateTime;

public record Journal(
        Integer id,
        LocalDateTime timeOut,
        LocalDateTime timeIn,
        Integer autoId,
        Integer routeId
) {}