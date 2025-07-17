package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorsimulator.cache.CategoryPreferenceWeightLocalCache;
import com.simpaylog.generatorsimulator.cache.CategorySpendingPatternLocalCache;
import com.simpaylog.generatorsimulator.dto.CategoryType;
import com.simpaylog.generatorsimulator.dto.PreferenceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

@Import({CategorySpendingPatternLocalCache.class, CategoryPreferenceWeightLocalCache.class, TransactionGenerator.class})
class TransactionGeneratorTest extends TestConfig {
    @Autowired
    TransactionGenerator transactionGenerator;

    private Map<CategoryType, List<LocalDateTime>> countMap;
    private final Set<CategoryType> TEST_TARGET_CATEGORIES = Set.of(CategoryType.values());

    @BeforeEach
    void setup() {
        countMap = new HashMap<>();
        for (CategoryType categoryType : CategoryType.values()) {
            countMap.put(categoryType, new ArrayList<>());
        }
    }

    @AfterEach
    void after() {
        System.out.println("카테고리별 한달 집계");
        printCategoryResult(countMap, TEST_TARGET_CATEGORIES);
    }

    @MethodSource("preferenceTypes")
    @ParameterizedTest(name = "[{index}] 성향: {0}")
    void test(PreferenceType preferenceType) {
        // Given
        LocalDateTime from = LocalDateTime.of(2025, 7, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 7, 31, 23, 59);
        Map<CategoryType, LocalDateTime> repeated = new HashMap<>();
        // When & Then
        long hours = ChronoUnit.HOURS.between(from, to);
        for (int i = 0; i <= hours; i++) {
            LocalDateTime current = from.plusHours(i);
            transactionGenerator.pickOneCategory(current, preferenceType, repeated)
                    .ifPresent(s -> countMap.get(s).add(current));
        }

    }

    static Stream<Arguments> preferenceTypes() {
        return Arrays.stream(PreferenceType.values()).map(Arguments::of);
    }

    private void printCategoryResult(Map<CategoryType, List<LocalDateTime>> countMap, Set<CategoryType> targets) {
        for (Map.Entry<CategoryType, List<LocalDateTime>> entry : countMap.entrySet()) {
            if (!targets.contains(entry.getKey())) continue;
            System.out.printf("%s: %d개%n", entry.getKey().getLabel(), entry.getValue().size());
            Collections.sort(entry.getValue());
        }
    }
}