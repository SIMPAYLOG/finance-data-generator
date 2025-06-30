package com.simpaylog.generatorsimulator.util;

import com.simpaylog.generatorapi.configuration.IncomeLevelLocalCache;
import com.simpaylog.generatorapi.dto.IncomeLevelInfo;
import com.simpaylog.generatorsimulator.TestConfig;
import com.simpaylog.generatorsimulator.configuration.PreferenceLocalCache;
import com.simpaylog.generatorsimulator.dto.ConsumptionDelta;
import com.simpaylog.generatorsimulator.dto.MonthlyConsumptionCost;
import com.simpaylog.generatorsimulator.dto.PreferenceInfos;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.YearMonth;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.simpaylog.generatorsimulator.util.ConsumptionDeltaAllocator.*;
import static org.junit.jupiter.api.Assertions.*;

@Import({IncomeLevelLocalCache.class, PreferenceLocalCache.class})
public class ConsumptionDeltaAllocatorTest extends TestConfig {
    @Autowired
    private PreferenceLocalCache preferenceLocalCache;
    @Autowired
    private IncomeLevelLocalCache incomeLevelLocalCache;

    private static final Set<String> TAG_FIELDS = Set.of(
            "groceriesNonAlcoholicBeverages",
            "alcoholicBeveragesTobacco",
            "clothingFootwear",
            "housingUtilitiesFuel",
            "householdGoodsServices",
            "health",
            "transportation",
            "communication",
            "recreationCulture",
            "education",
            "foodAccommodation",
            "otherGoodsServices"
    );

    @RepeatedTest(value = 5, name = "{displayName} - {currentRepetition}/{totalRepetitions}")
    @DisplayName("소비증감량 랜덤 추출 로직 점검")
    void totalConsumeRangeTest(RepetitionInfo repetitionInfo) {
        int preferenceId = repetitionInfo.getCurrentRepetition();
        PreferenceInfos preferenceInfos = preferenceLocalCache.get(preferenceId);
        int min = preferenceInfos.totalConsumeRange().min();
        int max = preferenceInfos.totalConsumeRange().max();
        for (int tc = 0; tc < 100; tc++) {
            int totalConsumeChange = getRandomConsumeDelta(min, max);
            assertTrue(min <= totalConsumeChange && totalConsumeChange <= max);
        }
    }

    @RepeatedTest(value = 10, name = "{displayName} - {currentRepetition}/{totalRepetitions}")
    @DisplayName("각 성향별 상세 태그 소비 증감량이 범위에 맞는 값들로 설정됐는지 검증")
    void tagConsumeDeltaWithinRangeTest(RepetitionInfo repetitionInfo) {
        int decile = repetitionInfo.getCurrentRepetition();
        IncomeLevelInfo incomeInfo = incomeLevelLocalCache.get(decile);

        for (int preferenceId = 1; preferenceId <= 5; preferenceId++) {
            PreferenceInfos preference = preferenceLocalCache.get(preferenceId);
            ConsumptionDelta tagDeltas = ConsumptionDeltaAllocator.getRandomTagConsumeDelta(incomeInfo, preference);

            Map<String, Function<ConsumptionDelta, Integer>> deltaMap = getDeltaGetters();
            int totalDeltaSum = 0;

            for (PreferenceInfos.TagConsumeRange tag : preference.tagConsumeRange()) {
                String type = tag.type();
                assertTrue(TAG_FIELDS.contains(type), "tag명 오류: " + type);

                int min = tag.min() + incomeInfoValue(incomeInfo, type);
                int max = tag.max() + incomeInfoValue(incomeInfo, type);
                int actual = deltaMap.get(type).apply(tagDeltas);

                assertTrue(actual >= min && actual <= max,
                        String.format("[%s] 현재값 = %d, 기준 범위 = [%d, %d]", type, actual, min, max));

                totalDeltaSum += actual - incomeInfoValue(incomeInfo, type);
            }

            int totalMin = preference.totalConsumeRange().min();
            int totalMax = preference.totalConsumeRange().max();

            assertTrue(totalDeltaSum >= totalMin && totalDeltaSum <= totalMax,
                    String.format("총 변화량 = %d, 기준 범위 = [%d, %d]", totalDeltaSum, totalMin, totalMax));
        }
    }

    private int incomeInfoValue(IncomeLevelInfo info, String tagName) {
        return switch (tagName) {
            case "groceriesNonAlcoholicBeverages" -> info.groceriesNonAlcoholicBeverages().intValue();
            case "alcoholicBeveragesTobacco" -> info.alcoholicBeveragesTobacco().intValue();
            case "clothingFootwear" -> info.clothingFootwear().intValue();
            case "housingUtilitiesFuel" -> info.housingUtilitiesFuel().intValue();
            case "householdGoodsServices" -> info.householdGoodsServices().intValue();
            case "health" -> info.health().intValue();
            case "transportation" -> info.transportation().intValue();
            case "communication" -> info.communication().intValue();
            case "recreationCulture" -> info.recreationCulture().intValue();
            case "education" -> info.education().intValue();
            case "foodAccommodation" -> info.foodAccommodation().intValue();
            case "otherGoodsServices" -> info.otherGoodsServices().intValue();
            default -> throw new IllegalArgumentException("Unknown tag: " + tagName);
        };
    }


    @RepeatedTest(value = 5, name = "{displayName} - {currentRepetition}/{totalRepetitions}")
    @DisplayName("상세 태그 퍼센트 증감량을 기반으로 소비 금액 계산")
    void calculateConsumptionTest(RepetitionInfo repetitionInfo){
        IncomeLevelInfo incomeLevelInfos = incomeLevelLocalCache.get(1);
        long income = 3_000_000; // 월 수입
        long originalTotalConsumptionCost = calcOriginalTotalConsumption(incomeLevelInfos, income); //성향X 소비지출 총 금액
        int preferenceId = repetitionInfo.getCurrentRepetition();
        PreferenceInfos preferenceInfos = preferenceLocalCache.get(preferenceId);
        ConsumptionDelta consumeDelta = getRandomTagConsumeDelta(incomeLevelInfos, preferenceInfos);

        //계산할 달(날짜) 지정
        int year = 2025;
        for(int month = 1; month <= 12; month++) {
            //각 월(+ 해당 월의 일 별) 지출량 계산
            MonthlyConsumptionCost monthly = calculateConsumption(consumeDelta, income, originalTotalConsumptionCost, YearMonth.of(year, month));
            //수입 == 저축 + 지출인지 확인
            assertEquals(income, monthly.totalConsumptionCost() + monthly.totalSurplusCost());
        }
    }
}
