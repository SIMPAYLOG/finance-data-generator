package com.simpaylog.generatorcore.cache.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simpaylog.generatorcore.dto.TimeUnit;
import com.simpaylog.generatorcore.enums.PreferenceType;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixedIncomePolicy(
        Map<PreferenceType, Map<Integer, AssignmentRule>> assignment,
        List<SourceTemplate> sources,
        Map<PreferenceType, Map<Integer, List<String>>> mapping
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AssignmentRule(
            double probability
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SourceTemplate(
            String category,
            String type,
            String name,
            Schedule schedule,
            List<Long> amountRange,
            double occurrenceProbability,
            Conditions conditions
    ) {}

    /**
     * 고정 주기 스케줄
     * - unit: DAYS/WEEKS/MONTHS/QUARTERS/YEARS
     * - dayOfMonth: 해당 월 지급일 (예: 15일)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Schedule(
            TimeUnit unit,
            int dayOfMonth
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Conditions(
            Integer decileMin,
            Integer decileMax,
            Integer ageMin,
            Integer ageMax
    ) {}
}