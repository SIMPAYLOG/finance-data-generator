package com.simpaylog.generatorsimulator.cache.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DecileStat(
        int decile,
        Map<String, BigDecimal> byCategory
) {
}
