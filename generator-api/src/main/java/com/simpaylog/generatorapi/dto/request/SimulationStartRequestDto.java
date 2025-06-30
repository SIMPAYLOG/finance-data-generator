package com.simpaylog.generatorapi.dto.request;

import java.time.LocalDate;

public record SimulationStartRequestDto(
        int userCount,
        LocalDate durationStart,
        LocalDate durationEnd
) {
}
