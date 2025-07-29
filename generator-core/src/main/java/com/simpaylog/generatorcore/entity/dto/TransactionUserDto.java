package com.simpaylog.generatorcore.entity.dto;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.enums.WageType;

import java.math.BigDecimal;

public record TransactionUserDto(
    Long userId,
    String sessionId,
    Integer decile,
    BigDecimal balance,
    Integer preferenceId,
    WageType wageType,
    Integer autoTransferDayOfMonth,
    String activeHour,
    BigDecimal incomeValue
) {

    public static TransactionUserDto fromEntity(User entity) {
        return new TransactionUserDto(
                entity.getId(),
                entity.getSessionId(),
                entity.getDecile(),
                entity.getBalance(),
                entity.getUserBehaviorProfile().getPreferenceId(),
                entity.getUserBehaviorProfile().getWageType(),
                entity.getUserBehaviorProfile().getAutoTransferDayOfMonth(),
                entity.getUserBehaviorProfile().getActiveHours(),
                entity.getUserBehaviorProfile().getIncomeValue()
        );
    }
}
