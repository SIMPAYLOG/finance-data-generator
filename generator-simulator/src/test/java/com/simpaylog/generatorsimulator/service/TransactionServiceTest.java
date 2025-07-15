package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorsimulator.cache.CategoryPreferenceWeightLocalCache;
import com.simpaylog.generatorsimulator.cache.CategorySpendingPatternLocalCache;
import com.simpaylog.generatorsimulator.cache.TradeInfoLocalCache;
import com.simpaylog.generatorsimulator.dto.CategoryType;
import com.simpaylog.generatorsimulator.dto.PreferenceType;
import com.simpaylog.generatorsimulator.dto.Trade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@Import({TransactionGenerator.class, TradeGenerator.class, CategoryPreferenceWeightLocalCache.class, CategorySpendingPatternLocalCache.class, TradeInfoLocalCache.class})
class TransactionServiceTest extends TestConfig {

    @Autowired
    private TransactionGenerator transactionGenerator;
    @Autowired
    private TradeGenerator tradeGenerator;

    @Test
    void test() {
        LocalDateTime from = LocalDateTime.of(2024, 7, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 7, 31, 23, 59);
        int decile = 1;
        PreferenceType preferenceType = PreferenceType.DEFAULT;
        Random random = new Random();
        int money = 0;
        Map<CategoryType, Integer> map = new HashMap<>();
        for(CategoryType ct : CategoryType.values()) map.put(ct, 0);
        for (; from.isBefore(to); from = from.plusHours(1)) {
            int cnt = random.nextInt(3);
            for (int i = 0; i < cnt; i++) {
                CategoryType picked = transactionGenerator.pickOneCategory(from, preferenceType).orElse(null);
                if (picked == null) {
                    //System.out.println("데이터 없음");
                    continue;
                }
                Trade result = tradeGenerator.generateTrade(decile, picked.getKey());
                money += result.cost();
                map.put(picked, map.get(picked) + result.cost());
                System.out.printf("[%s] %s %s %d원 지출%n", from, picked.getLabel(), result.tradeName(), result.cost());

            }

        }
        System.out.printf("총 사용 금액: %d%n", money);
        for(Map.Entry<CategoryType, Integer> entry: map.entrySet()) {
            System.out.printf("[%s]: %d원 사용%n", entry.getKey().getLabel(), entry.getValue());
        }

    }

}