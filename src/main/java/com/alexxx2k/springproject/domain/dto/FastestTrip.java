// FastestTrip.java
package com.alexxx2k.springproject.domain.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public record FastestTrip(
        Integer autoId,
        String autoNum,
        String autoMark,
        String routeName,
        Duration tripDuration,
        LocalDateTime timeOut,
        LocalDateTime timeIn
) {}
