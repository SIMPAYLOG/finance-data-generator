package com.simpaylog.generatorcore.cache.dto.preference;

import java.math.BigDecimal;
import java.util.List;

public record PreferenceInfo(
        int id,
        String name,
        TotalConsumeRange totalConsumeRange,
        List<TagConsumeRange> tagConsumeRange)
{
    public record TotalConsumeRange(BigDecimal min, BigDecimal max) {}
    public record TagConsumeRange(String type, String typeKor, BigDecimal min, BigDecimal max) {}
}
