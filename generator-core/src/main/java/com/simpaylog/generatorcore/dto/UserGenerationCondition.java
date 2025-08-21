package com.simpaylog.generatorcore.dto;

public record UserGenerationCondition(
        int id,
        int userCount,
        String preferenceId,
        String ageGroup,
        String gender,
        String occupationCode
) {
}
