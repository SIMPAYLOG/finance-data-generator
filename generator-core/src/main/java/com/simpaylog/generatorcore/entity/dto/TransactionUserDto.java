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
    Integer occupationCode,
    String occupationName,
    PreferenceType preferenceType,
    WageType wageType,
    String activeHour,
    BigDecimal incomeValue,
    BigDecimal savingRate,
    Integer locationId
) {

}
