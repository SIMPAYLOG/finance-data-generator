package com.simpaylog.generatorapi.dto.request;

import java.time.LocalDateTime;

public record SimulationStartRequestDto(
        Long userCount,
        LocalDateTime durationStart,
        LocalDateTime durationEnd
) {

    public static SimulationStartRequestDto of(Long userCount, LocalDateTime durationStart, LocalDateTime durationEnd) {
        return new SimulationStartRequestDto(userCount, durationStart, durationEnd);
    }
}
