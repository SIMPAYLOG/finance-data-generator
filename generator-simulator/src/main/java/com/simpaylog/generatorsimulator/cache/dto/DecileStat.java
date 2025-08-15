package com.simpaylog.generatorsimulator.cache.dto;

import com.simpaylog.generatorcore.dto.CategoryType;

import java.math.BigDecimal;
import java.util.Map;

public record DecileStat(
        int decile,
        BigDecimal averageDisposableIncome,
        Map<CategoryType, BigDecimal> byCategory
) {
}
