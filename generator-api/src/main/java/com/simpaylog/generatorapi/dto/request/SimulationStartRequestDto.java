package com.simpaylog.generatorapi.dto.request;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public record SimulationStartRequestDto(
        @Valid List<UserGenerationConditionRequestDto> conditions,
        LocalDate durationStart,
        LocalDate durationEnd
) {
}
