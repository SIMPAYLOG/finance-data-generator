package com.simpaylog.generatorsimulator.util;

import com.simpaylog.generatorcore.cache.IncomeLevelLocalCache;
import com.simpaylog.generatorcore.cache.PreferenceLocalCache;
import com.simpaylog.generatorcore.cache.dto.IncomeLevelInfo;
import com.simpaylog.generatorcore.cache.dto.preference.ConsumptionDelta;
import com.simpaylog.generatorcore.cache.dto.preference.MonthlyConsumption;
import com.simpaylog.generatorcore.cache.dto.preference.PreferenceInfo;
import com.simpaylog.generatorsimulator.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Set;

import static com.simpaylog.generatorsimulator.util.ConsumptionAllocator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(PreferenceLocalCache.class)
public class ConsumptionAllocatorTest extends TestConfig {
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
    @Autowired
    private PreferenceLocalCache preferenceLocalCache;
    @Autowired
    private IncomeLevelLocalCache incomeLevelLocalCache;

    @RepeatedTest(value = 5, name = "{displayName} - {currentRepetition}/{totalRepetitions}")
    @DisplayName("소비증감량 랜덤 추출 로직 점검")
    void totalConsumeRangeTest(RepetitionInfo repetitionInfo) {
        int preferenceId = repetitionInfo.getCurrentRepetition();
        PreferenceInfo preferenceInfo = preferenceLocalCache.get(preferenceId);
        BigDecimal min = preferenceInfo.totalConsumeRange().min();
        BigDecimal max = preferenceInfo.totalConsumeRange().max();
        for (int tc = 0; tc < 100; tc++) {
            int totalConsumeChange = getRandomConsumeDelta(min, max);
            assertTrue(min.intValue() <= totalConsumeChange && totalConsumeChange <= max.intValue(),
                    String.format("현재 태그별 변화량 = %d, 기준 범위 = [%s, %s]", totalConsumeChange, min, max));
        }
    }

    @RepeatedTest(value = 5, name = "{displayName} - {currentRepetition}/{totalRepetitions}")
    @DisplayName("각 성향별 상세 태그 소비 증감량이 범위에 맞는 값들로 설정됐는지 검증")
    void tagConsumeDeltaWithinRangeTest(RepetitionInfo repetitionInfo) {
        int preferenceId = repetitionInfo.getCurrentRepetition(); //소득 분위(incomdeLevelCache의 id값)
        PreferenceInfo preference = preferenceLocalCache.get(preferenceId);
        ConsumptionDelta tagDeltas = ConsumptionAllocator.getRandomTagConsumeDelta(preference);

        BigDecimal totalDeltaSum = BigDecimal.ZERO;

        for (PreferenceInfo.TagConsumeRange tag : preference.tagConsumeRange()) {
            String type = tag.type();
            assertTrue(TAG_FIELDS.contains(type), "tag명 오류: " + type);

            BigDecimal min = tag.min();
            BigDecimal max = tag.max();
            BigDecimal actual = getConsumptionDeltasValue(tagDeltas, type);
            assertTrue(actual.compareTo(min) >= 0 && actual.compareTo(max) <= 0,
                    String.format("[%s] 현재값 = %s, 기준 범위 = [%s, %s]", type, actual, min, max));
            totalDeltaSum = totalDeltaSum.add(actual);
        }

        //총 소비 변화율 합 확인
        assertEquals(0, totalDeltaSum.compareTo(tagDeltas.totalDelta()));
        BigDecimal totalMin = preference.totalConsumeRange().min();
        BigDecimal totalMax = preference.totalConsumeRange().max();

        assertTrue(totalDeltaSum.compareTo(totalMin) >= 0 && totalDeltaSum.compareTo(totalMax) <= 0,
                String.format("총 변화량 = %s, 기준 범위 = [%s, %s]", totalDeltaSum, totalMin, totalMax));
    }

    @RepeatedTest(value = 5, name = "{displayName} - {currentRepetition}/{totalRepetitions}")
    @DisplayName("상세 태그 퍼센트 증감량을 기반으로 소비 금액 계산")
    void createNewConsumptionTest(RepetitionInfo repetitionInfo) {
        int preferenceId = repetitionInfo.getCurrentRepetition();
        PreferenceInfo preferenceInfo = preferenceLocalCache.get(preferenceId);
        IncomeLevelInfo incomeLevelInfos = incomeLevelLocalCache.get(1);
        BigDecimal income = BigDecimal.valueOf(3_000_000); // 월 수입
        ConsumptionDelta consumeDelta = getRandomTagConsumeDelta(preferenceInfo);

        int year = 2025;
        for (int month = 1; month <= 12; month++) {
            //각 월(+ 해당 월의 일 별) 지출량 계산
            MonthlyConsumption monthly = createNewConsumption(consumeDelta, incomeLevelInfos, income, YearMonth.of(year, month));
            //수입 == 저축 + 지출인지 확인
            BigDecimal predictIncome = monthly.monthlyTotalConsumption().add(monthly.monthlyTotalSurplus());
            assertEquals(0, income.compareTo(predictIncome));
            //한 달치 데이터가 모두 들어갔는지 확인
            assertEquals(monthly.DailyConsumptionList().size(), YearMonth.of(year, month).lengthOfMonth(),
                    String.format("%d || %d", monthly.DailyConsumptionList().size(), YearMonth.of(year, month).lengthOfMonth()));
        }
    }
}
