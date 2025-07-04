package com.simpaylog.generatorapi.dto.request;

import jakarta.validation.Valid;

import java.util.List;

public record CreateUserRequestDto(
        @Valid List<UserGenerationConditionRequestDto> conditions
) {
}
