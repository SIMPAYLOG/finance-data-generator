package com.simpaylog.generatorcore.entity.dto;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.WageType;

import java.math.BigDecimal;

public record TransactionUserDto(
    Long userId,
    String sessionId,
    Integer decile,
    Integer age,
    PreferenceType preferenceType,
    WageType wageType,
    String activeHour,
    BigDecimal incomeValue,
    BigDecimal savingRate
) {

    public static TransactionUserDto fromEntity(User entity) {
        return new TransactionUserDto(
                entity.getId(),
                entity.getSessionId(),
                entity.getDecile(),
                entity.getAge(),
                entity.getUserBehaviorProfile().getPreferenceType(),
                entity.getUserBehaviorProfile().getWageType(),
                entity.getUserBehaviorProfile().getActiveHours(),
                entity.getUserBehaviorProfile().getIncomeValue(),
                entity.getUserBehaviorProfile().getSavingRate()
        );
    }
}
