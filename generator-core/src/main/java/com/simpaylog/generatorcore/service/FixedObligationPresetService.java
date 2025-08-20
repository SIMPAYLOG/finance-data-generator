package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.DecileStatsLocalCache;
import com.simpaylog.generatorcore.cache.FixedIncomeLocalCache;
import com.simpaylog.generatorcore.cache.dto.FixedIncomePolicy;
import com.simpaylog.generatorcore.cache.dto.TradeInfo;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorcore.dto.FixedObligation;
import com.simpaylog.generatorcore.dto.TimeUnit;
import com.simpaylog.generatorcore.dto.UserProfile;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.TransactionType;
import com.simpaylog.generatorcore.utils.MoneyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixedObligationPresetService implements FixedObligationPresetProvider {

    private final FixedIncomeLocalCache fixedIncomeLocalCache;
    private final DecileStatsLocalCache decileStatsLocalCache;
    private final HousingBundleService housingBundleService;

    @Override
    public List<FixedObligation> generate(UserProfile profile, LocalDate anchorDate) {
        List<FixedObligation> out = new ArrayList<>();
        String effectiveFrom = anchorDate.toString(); // yyyy-MM-dd
        //1. 주거 공공요금
        out.addAll(generateHousing(profile, effectiveFrom));
        //2. 통신 요금
        out.addAll(generateTelecom(profile, effectiveFrom));
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
                case "월세" -> out.add(monthlyWithdraw(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.adjust(BigDecimal.valueOf(chosen), 10000, RoundingMode.HALF_UP), 1,
                        "월세", tenureType, effectiveFrom
                ));
                case "관리비" -> out.add(monthlyWithdraw(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.roundTo10(BigDecimal.valueOf(chosen)), 15,
                        "관리비", null, effectiveFrom
                ));
                case "전기 요금" -> out.add(monthlyWithdraw(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.roundTo10(BigDecimal.valueOf(chosen)), 15,
                        "전기 요금", null, effectiveFrom
                ));
                case "수도 요금" -> out.add(monthlyWithdraw(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.roundTo10(BigDecimal.valueOf(chosen)), 15,
                        "수도 요금", null, effectiveFrom
                ));
                case "도시가스 요금" -> out.add(monthlyWithdraw(
                        profile.userId(), CategoryType.HOUSING_UTILITIES_FUEL, MoneyUtil.roundTo10(BigDecimal.valueOf(chosen)), 15,
                        "도시가스 요금", null, effectiveFrom
                ));
                default -> { /* 알 수 없는 항목은 스킵 */ }
            }
        }
        return out;
    }

    private List<FixedObligation> generateTelecom(UserProfile profile, String effectiveFrom) {
        List<FixedObligation> out = new ArrayList<>();
        // 1) 주 회선(필수)
        BigDecimal mainAmount = BigDecimal.valueOf(calcMainMobileAmount(profile));
        out.add(monthlyWithdraw(profile.userId(), CategoryType.COMMUNICATION, mainAmount, 25, "통신 요금(주 회선)", null, effectiveFrom));

        // 2) 추가 회선 (확률)
        additionalMobileLine(out, profile, effectiveFrom);

        // 3) 데이터 전용 기기 (확률)
        addCellularDevice(out, profile, effectiveFrom);

        // 4) 브로드 밴드 IPTV (확률)
        broadbandAndIpTv(out, profile, effectiveFrom);
        return out;
    }

    private int calcMainMobileAmount(UserProfile profile) {
        int decile = profile.decile();
        int age = profile.ageGroup();
        PreferenceType pref = profile.preferenceType();

        // 1. 소득분위 기반 범위 설정
        double base;
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

        return MoneyUtil.roundTo10(BigDecimal.valueOf(base * ageFactor * prefFactor)).intValue();
    }

    private void additionalMobileLine(List<FixedObligation> out, UserProfile profile, String effectiveFrom) {
        // 기본 확률: 20대 0.25 / 30대 0.45 / 40대+ 0.60
        double p = (profile.ageGroup() >= 40 ? 0.60 : (profile.ageGroup() >= 30 ? 0.45 : 0.25));
        // 성향 보정: 절약 -0.10 / 소비 +0.10
        if (profile.preferenceType() == PreferenceType.SAVING_ORIENTED) p -= 0.10;
        if (profile.preferenceType() == PreferenceType.CONSUMPTION_ORIENTED) p += 0.10;

        if (shouldAdd(p)) {
            BigDecimal amount = MoneyUtil.roundTo10(BigDecimal.valueOf(25_000 + ThreadLocalRandom.current().nextDouble() * 20_000)); // 금액: 25,000 ~ 45,000
            out.add(monthlyWithdraw(profile.userId(), CategoryType.COMMUNICATION, amount, 27, "통신 요금(추가 회선)", null, effectiveFrom));
        }
    }

    private void addCellularDevice(List<FixedObligation> out, UserProfile profile, String effectiveFrom) {
        // 20대: 0.35 / 30대: 0.25 / 그 외 0.15
        double dataDevP = (profile.ageGroup() == 20 ? 0.35 : (profile.ageGroup() == 30 ? 0.25 : 0.15));
        if (profile.preferenceType() == PreferenceType.SAVING_ORIENTED) dataDevP -= 0.05;
        if (profile.preferenceType() == PreferenceType.CONSUMPTION_ORIENTED) dataDevP += 0.05;

        if (shouldAdd(dataDevP)) {
            BigDecimal dataDevAmt = MoneyUtil.roundTo10(BigDecimal.valueOf(10000 + ThreadLocalRandom.current().nextDouble() * 15000)); // 10000~25000원
            out.add(monthlyWithdraw(profile.userId(), CategoryType.COMMUNICATION, dataDevAmt, 12, "데이터 전용 기기 요금", null, effectiveFrom));
        }
    }

    private void broadbandAndIpTv(List<FixedObligation> out, UserProfile profile, String effectiveFrom) {
        FixedObligation broadband = null;
        FixedObligation iptv = null;
        double bbP = (profile.decile() >= 5 ? 0.70 : 0.50);
        if (profile.preferenceType() == PreferenceType.SAVING_ORIENTED) bbP -= 0.10;
        if (profile.preferenceType() == PreferenceType.CONSUMPTION_ORIENTED) bbP += 0.10;

        // broadband
        boolean hasBroadband = false;
        if (shouldAdd(bbP)) {
            hasBroadband = true;
            BigDecimal bbAmt = MoneyUtil.roundTo10(BigDecimal.valueOf(35000 + ThreadLocalRandom.current().nextDouble() * 20000)); // 35000~55000
            broadband = monthlyWithdraw(profile.userId(), CategoryType.COMMUNICATION, bbAmt, 21, "가정용 인터넷 요금", null, effectiveFrom);
            out.add(broadband);
        }

        // IPTV (브로드밴드 있을 때만 확률)
        if (hasBroadband) {
            double iptvP = 0.40;
            if (profile.preferenceType() == PreferenceType.SAVING_ORIENTED) iptvP -= 0.10;
            if (profile.preferenceType() == PreferenceType.CONSUMPTION_ORIENTED) iptvP += 0.10;

            if (shouldAdd(iptvP)) {
                BigDecimal iptvAmt = MoneyUtil.roundTo10(BigDecimal.valueOf(10000 + ThreadLocalRandom.current().nextDouble() * 10000)); // 10k~20k
                iptv = monthlyWithdraw(profile.userId(), CategoryType.COMMUNICATION, iptvAmt, 21, "IPTV/케이블 요금", null, effectiveFrom);
                out.add(iptv);
            }
        }
        // 결합할인
        if (broadband != null && iptv != null) {
            double base = 0.10;
            if (profile.preferenceType() == PreferenceType.CONSUMPTION_ORIENTED) base += 0.02;
            if (profile.preferenceType() == PreferenceType.SAVING_ORIENTED) base -= 0.02;

            // IPTV 금액에서만 할인 적용(한 군데에 몰아 깎기: 설명/추적 쉬움)
            BigDecimal discount = iptv.amount().multiply(BigDecimal.valueOf(base));
            BigDecimal newIptv = MoneyUtil.roundTo10(iptv.amount().subtract(discount));

            // 리스트에 들어간 IPTV 객체를 교체(간단히 remove+add 또는 새로 생성)
            out.remove(iptv);
            out.add(new FixedObligation(
                    iptv.userId(),
                    iptv.categoryType(),
                    iptv.transactionType(),
                    newIptv,
                    iptv.recurrence(),
                    iptv.tenureType(),
                    iptv.description() + " (결합할인)",
                    iptv.effectiveFrom(),
                    iptv.effectiveTo()
            ));

        }
    }

    private FixedObligation monthlyWithdraw(long userId,
                                            CategoryType category,
                                            BigDecimal amount,
                                            Integer dayOfMonth,
                                            String desc,
                                            FixedObligation.TenureType tenureOrNull,
                                            String effectiveFrom) {

        var rec = new FixedObligation.IntervalRecurrence(
                TimeUnit.MONTHS,
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

    private boolean shouldAdd(double probability) {
        return ThreadLocalRandom.current().nextDouble() < clamp(probability);
    }

    private double clamp(double p) {
        return Math.max(0.0, Math.min(1.0, p));
    }

}
