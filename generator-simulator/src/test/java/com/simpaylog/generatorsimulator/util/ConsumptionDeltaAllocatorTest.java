package com.simpaylog.generatorsimulator.util;

import com.simpaylog.generatorsimulator.configuration.IncomeLevelLocalCache;
import com.simpaylog.generatorsimulator.configuration.PreferenceLocalCache;
import com.simpaylog.generatorsimulator.dto.DailyConsumptionCost;
import com.simpaylog.generatorsimulator.dto.IncomeLevelInfos;
import com.simpaylog.generatorsimulator.dto.MonthlyConsumptionCost;
import com.simpaylog.generatorsimulator.dto.PreferenceInfos;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.YearMonth;
import java.util.*;

import static com.simpaylog.generatorsimulator.util.ConsumptionDeltaAllocator.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({IncomeLevelLocalCache.class, PreferenceLocalCache.class})
public class ConsumptionDeltaAllocatorTest {
    @Autowired
    private PreferenceLocalCache preferenceLocalCache;
    @Autowired
    private IncomeLevelLocalCache incomeLevelLocalCache;
    private Map<Integer, PreferenceInfos> preferences;
    private Map<Integer, IncomeLevelInfos> incomeLevelCache;

    @BeforeEach
    void init() {
        preferences = preferenceLocalCache.getAll();
        incomeLevelCache = incomeLevelLocalCache.getAll();
    }

    @Test
    @DisplayName("소비증감량 랜덤 추출 로직 점검")
    void totalConsumeRangeTest() {
        for (int i = 1; i <= 5; i++) {
            PreferenceInfos preferenceInfos = preferences.get(i);
            int min = preferenceInfos.totalConsumeRange().min();
            int max = preferenceInfos.totalConsumeRange().max();
            for (int tc = 0; tc < 100; tc++) {
                int totalConsumeChange = getRandomConsumeDelta(min, max);
                assertTrue(min <= totalConsumeChange && totalConsumeChange <= max);
            }
        }
    }

    @Test
    @DisplayName("각 성향별 상세태그 소비 증감량 정하기")
    void tagConsumeRangeTest() {
        Map<String, Integer> tagDeltas;
        IncomeLevelInfos incomeLevelInfos = incomeLevelCache.get(1);
        for (int i = 1; i <= 5; i++) {
            PreferenceInfos preferenceInfos = preferences.get(i);
            int min = preferenceInfos.totalConsumeRange().min();
            int max = preferenceInfos.totalConsumeRange().max();
            List<PreferenceInfos.TagConsumeRange> tagRanges = preferenceInfos.tagConsumeRange();
            for (int tc = 0; tc < 10000; tc++) {
                tagDeltas = getRandomTagConsumeDelta(incomeLevelInfos, preferenceInfos); //test
                int sum = 0;
                assertNotNull(tagDeltas);
                for (int t = 0; t < 12; t++) {
                    String tag = tagRanges.get(t).type();
                    int tagDelta = tagDeltas.get(tag);
                    assertTrue(tagRanges.get(t).min() + incomeLevelInfos.getCost(tag).intValue() <= tagDelta && tagDelta <= tagRanges.get(t).max() + incomeLevelInfos.getCost(tag).intValue());
                    sum += tagDelta;
                }
                int totalDelta = tagDeltas.get("totalDelta");
                int totalChangedDelta = tagDeltas.get("totalChangedDelta");
                assertEquals(sum, totalDelta);
                assertTrue(min <= totalChangedDelta && totalChangedDelta <= max);
            }
        }
    }

    @Test
    @DisplayName("상세 태그 퍼센트 증감량을 기반으로 소비 금액 계산")
    void calculateConsumptionTest(){
        IncomeLevelInfos incomeLevelInfos = incomeLevelCache.get(1);
        long income = 4_000_000; // 월 수입
        long originalTotalConsumptionCost = calcOriginalTotalConsumption(incomeLevelInfos, income); //성향X 소비지출 총 금액
        System.out.printf("수입 : %d || 기존 지출 : %d\n", income, originalTotalConsumptionCost);
        for (int i = 1; i <= 5; i++) {
            PreferenceInfos preferenceInfos = preferences.get(i);
            Map<String, Integer> consumeDeltaData = getRandomTagConsumeDelta(incomeLevelInfos, preferenceInfos);

            //계산할 달(날짜) 지정
            int year = 2025;
            for(int month = 1; month <= 1; month++) {
                //각 월의 일별 지출량 계산
                MonthlyConsumptionCost results = calculateConsumption(consumeDeltaData, income, originalTotalConsumptionCost, YearMonth.of(year, month));

                //수입 == 저축 + 지출인지 확인
                assertEquals(income, results.getTotalConsumptionCost() + results.getTotalSurplusCost());

                //모든 지출의 합이 총 지출값과 같은지 확인
                long costSum = getCostSum(results);
                assertEquals(costSum, results.getTotalConsumptionCost());
                System.out.printf(preferenceInfos.name() + "의 총 소비지출 : %d || 기존과의 변화량 : 약 %d\n", results.getTotalConsumptionCost(), consumeDeltaData.get("totalDelta"));
            }

        }
    }

    private static long getCostSum(MonthlyConsumptionCost results) {
        long costSum = 0;
        for (DailyConsumptionCost result : results.getDailyConsumptionCostList()) {
            costSum += result.getClothingFootwear() + result.getAlcoholicBeveragesTobacco() + result.getGroceriesNonAlcoholicBeverages()
                    + result.getEducation() + result.getCommunication() + result.getFoodAccommodation()
                    + result.getHealth() + result.getHousingUtilitiesFuel() + result.getOtherGoodsServices()
                    + result.getRecreationCulture() + result.getHouseholdGoodsServices() + result.getTransportation();
        }
        return costSum;
    }
}
