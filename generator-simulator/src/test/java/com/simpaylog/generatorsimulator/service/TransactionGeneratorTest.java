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
import java.util.*;
import java.util.stream.Stream;

@Import({CategorySpendingPatternLocalCache.class, CategoryPreferenceWeightLocalCache.class, TransactionGenerator.class})
class TransactionGeneratorTest extends TestConfig {
    @Autowired TransactionGenerator transactionGenerator;
    Map<CategoryType, List<LocalDateTime>> countMap;
    @BeforeEach
    void setup() {
        countMap = new HashMap<>();
        for(CategoryType categoryType : CategoryType.values()) {
            countMap.put(categoryType, new ArrayList<>());
        }
    }
    @AfterEach
    void after() {
        String[] days = {" ", "월", "화", "수", "목", "금", "토", "일"};
        for(Map.Entry<CategoryType, List<LocalDateTime>> entry : countMap.entrySet()) {
            if(!entry.getKey().equals(CategoryType.TRANSPORTATION)) continue;
            System.out.printf("%s: %d개%n",entry.getKey().getLabel(), entry.getValue().size());
            Collections.sort(entry.getValue());
//            for(LocalDateTime localDateTime : entry.getValue()) {
//                System.out.println(localDateTime + " " + days[localDateTime.getDayOfWeek().getValue()]);
//            }
//        System.out.println();
        }
    }

    //@RepeatedTest(10)
    @ParameterizedTest
    @MethodSource("preferenceTypes")
    void test(PreferenceType preferenceType) {
        // Given
        LocalDateTime from = LocalDateTime.of(2025, 7, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 7, 31, 23, 59);
        // When & Then
        for(; from.isBefore(to); from = from.plusHours(1)) {
            //System.out.println(from.getHour() +"시 가능한 카테고리");
            //System.out.print("선택된 카테고리: ");
            LocalDateTime finalFrom = from;
            transactionGenerator.pickOneCategory(from, preferenceType).ifPresentOrElse(s -> {
                countMap.get(s).add(finalFrom);
                //System.out.println(s);
            }, () -> System.out.println("데이터 없음"));
        }
            System.out.print("["+preferenceType.getName() + "] ");

    }

    static Stream<Arguments> preferenceTypes() {
        return Arrays.stream(PreferenceType.values()).map(Arguments::of);
    }
}