package com.simpaylog.generatorcore.dto;

import com.simpaylog.generatorcore.enums.PreferenceType;

public record UserProfile(
        long userId,
        int decile,
        PreferenceType preferenceType,
        int ageGroup
//        boolean hasCar
) {
}
