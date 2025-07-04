package com.simpaylog.generatorcore.service.dto;

public record UserGenerationCondition(
        int userCount,
        String preferenceId,
        String ageGroup,
        String gender,
        String occupationCode
) {
}
