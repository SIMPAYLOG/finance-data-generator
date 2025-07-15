package com.simpaylog.generatorsimulator.cache.dto;

import com.simpaylog.generatorsimulator.dto.CategoryType;
import com.simpaylog.generatorsimulator.dto.PreferenceType;
import com.simpaylog.generatorsimulator.dto.SpendingFrequency;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record CategorySpendingPattern(
        Map<CategoryType, Config> categoryPatterns
) {
    public CategorySpendingPattern {
        categoryPatterns = Map.copyOf(categoryPatterns);
    }

    public record Config(
            Pattern defaultPattern, // frequency, weekdayWeights 만 존재
            Map<PreferenceType, Pattern> byPreference
    ) {}

    public record Pattern(
            SpendingFrequency frequency,
            List<Double> weekdayWeights,
            List<Double> hourWeights,
            Integer fixedDay,
            Integer eventCount,
            Double activeThreshold
    ) {}


    public Pattern getEffectivePattern(CategoryType category, PreferenceType preference) {
        Config config = categoryPatterns.get(category);
        if (config == null) return null;
        if (config.byPreference() != null && config.byPreference().containsKey(preference)) {
            return config.byPreference().get(preference);
        }
        return config.defaultPattern(); // 특정 성향이 보이지 않을 경우
    }

    public boolean hasCategory(String category) {
        CategoryType categoryType = CategoryType.fromKey(category);
        if(categoryType == null) return false;
        return true;
    }

    public Set<CategoryType> getAvailableCategories() {
        return categoryPatterns.keySet();
    }

}