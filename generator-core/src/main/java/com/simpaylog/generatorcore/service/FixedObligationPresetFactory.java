package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.DecileStatsLocalCache;
import com.simpaylog.generatorcore.cache.dto.TradeInfo;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorcore.dto.FixedObligation;
import com.simpaylog.generatorcore.dto.UserProfile;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.TransactionType;
import com.simpaylog.generatorcore.utils.MoneyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FixedObligationPresetFactory implements FixedObligationPresetProvider {

    private final DecileStatsLocalCache decileStatsLocalCache;
    private final HousingBundleService housingBundleService;
    // 스케일 조절

    @Override
    public List<FixedObligation> generate(UserProfile profile, LocalDate anchorDate) {
        List<FixedObligation> out = new ArrayList<>();
        String effectiveFrom = anchorDate.toString(); // yyyy-MM-dd
        //1. 주거 공공요금
        out.addAll(generateHousing(profile, effectiveFrom));

        //2. 통신 요금
        out.add(generateTelecom(profile, effectiveFrom));
        // TODO: 3. 보험, 구독, 교통, 저축, 자동 이체

        return out;
    }

    //1. 주거 및 공공 요금(주거, 수도, 광열)
    private List<FixedObligation> generateHousing(UserProfile profile, String effectiveFrom) {
        FixedObligation.TenureType tenureType = TenureTypeDeriver.derive(profile.decile(), profile.ageGroup());
        int targetTotal = decileStatsLocalCache.getDecileStat(profile.decile()).byCategory().get(CategoryType.HOUSING_UTILITIES_FUEL).intValue();
        List<FixedObligation> out = new ArrayList<>();
        List<TradeInfo.TradeItemDetail> ranges = housingBundleService.build(profile.decile(), profile.decile(), tenureType, profile.preferenceType(), targetTotal);
        for (TradeInfo.TradeItemDetail item : ranges) {
            int chosen = (item.min() + item.max()) / 2;
            switch (item.name()) {
                case "월세" -> out.add(monthly(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.adjust(BigDecimal.valueOf(chosen), 10000, RoundingMode.HALF_UP), 1,
                        "월세", tenureType, effectiveFrom
                ));
                case "관리비" -> out.add(monthly(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.roundTo10(BigDecimal.valueOf(chosen)), 15,
                        "관리비", null, effectiveFrom
                ));
                case "전기 요금" -> out.add(monthly(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.roundTo10(BigDecimal.valueOf(chosen)), 15,
                        "전기 요금", null, effectiveFrom
                ));
                case "수도 요금" -> out.add(monthly(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.roundTo10(BigDecimal.valueOf(chosen)), 15,
                        "수도 요금", null, effectiveFrom
                ));
                case "도시가스 요금" -> out.add(monthly(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.roundTo10(BigDecimal.valueOf(chosen)), 15,
                        "도시가스 요금", null, effectiveFrom
                ));
                default -> { /* 알 수 없는 항목은 스킵 */ }
            }
        }
        return out;
    }

    private FixedObligation generateTelecom(UserProfile profile, String effectiveFrom) {
        int decile = profile.decile();
        int age = profile.ageGroup();
        PreferenceType pref = profile.preferenceType();

        double base;

        // 1. 소득분위 기반 범위 설정
        if (decile <= 3) base = 20_000 + Math.random() * 20_000;       // 20,000 ~ 40,000
        else if (decile <= 6) base = 40_000 + Math.random() * 20_000;  // 40,000 ~ 60,000
        else if (decile <= 8) base = 60_000 + Math.random() * 30_000;  // 60,000 ~ 90,000
        else base = 80_000 + Math.random() * 50_000;                   // 80,000 ~ 130,000

        // 2. 연령대 보정
        double ageFactor = switch (age) {
            case 20 -> 1.15;  // 20대: 데이터 위주라 더 높음
            case 30 -> 1.05;
            case 40 -> 1.0;
            case 50 -> 0.9;
            default -> 0.85;  // 60대 이상
        };

        // 3. 성향 보정
        double prefFactor = switch (pref) {
            case CONSUMPTION_ORIENTED -> 1.15;
            case SAVING_ORIENTED -> 0.85;
            case UNPLANNED -> 0.8 + Math.random() * 0.6; // 0.8 ~ 1.4
            default -> 1.0;
        };

        // 4. 최종 금액
        int telecom = MoneyUtil.roundTo10(BigDecimal.valueOf(base * ageFactor * prefFactor)).intValue();

        return monthly(profile.userId(),
                CategoryType.COMMUNICATION,
                BigDecimal.valueOf(telecom),
                25, // 보통 20~말일, 여기선 25일 고정
                "통신 요금",
                null,
                effectiveFrom);
    }


    private FixedObligation monthly(long userId,
                                    CategoryType category,
                                    BigDecimal amount,
                                    Integer dayOfMonth,     // null이면 말일
                                    String desc,
                                    FixedObligation.TenureType tenureOrNull,
                                    String effectiveFrom) {
        // 앵커 생성 (dayOfMonth == null → 말일)
        //String anchor = makeAnchor(effectiveFrom, dayOfMonth, "09:00");

        var rec = new FixedObligation.IntervalRecurrence(
                FixedObligation.TimeUnit.MONTHS,
                1,
                null,                 // dayOfWeek (주간만 씀)
                dayOfMonth,           // dayOfMonth (null이면 말일)
                (dayOfMonth == null)  // lastDayOfMonth (dayOfMonth 없으면 true)
        );

        return new FixedObligation(
                userId,
                category.getKey(),
                TransactionType.WITHDRAW,
                amount,
                rec,
                tenureOrNull, // 월세만 채움, 유틸은 null
                desc,
                effectiveFrom,
                null
        );
    }

    /** "yyyy-MM-dd" + (일자 or 말일) + "HH:mm" → "yyyy-MM-dd'T'HH:mm" */
//    private static String makeAnchor(String ymd, Integer dayOfMonthOrNull, String timeHHmm) {
//        LocalDate base = LocalDate.parse(ymd);
//
//        LocalDate date = (dayOfMonthOrNull == null)
//                ? base.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
//                : base.withDayOfMonth(Math.min(dayOfMonthOrNull, base.lengthOfMonth()));
//
//        LocalTime time = LocalTime.parse(timeHHmm);
//        return LocalDateTime.of(date, time).toString(); // 예: 2025-09-15T09:00
//    }
}
