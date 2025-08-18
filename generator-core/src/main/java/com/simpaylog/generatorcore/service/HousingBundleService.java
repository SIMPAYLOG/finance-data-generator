package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.cache.dto.TradeInfo;
import com.simpaylog.generatorcore.dto.FixedObligation;
import com.simpaylog.generatorcore.enums.PreferenceType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HousingBundleService {

    private static final int M0 = 45_000; // 관리비
    private static final int E0 = 30_000; // 전기
    private static final int W0 = 15_000; // 수도
    private static final int G0 = 35_000; // 가스
    private static final double ALPHA_REF = 0.039; // ≈ 3.9%

    public List<TradeInfo.TradeItemDetail> build(int decile, int ageGroup, FixedObligation.TenureType tenure, PreferenceType pref, int targetTotal) {
        // 1) 성향 계수 로드(월세에는 적용하지 않음)
        UtilityTuning t = tuningOf(pref);

        // 2) 유틸 네 가지 계산(천 원 반올림)
        int M = roundK((int) Math.round(M0 * t.mMaint()));
        int E = roundK((int) Math.round(E0 * t.mElec()));
        int W = roundK((int) Math.round(W0 * t.mWater()));
        int G = roundK((int) Math.round(G0 * t.mGas()));
        int utilities = M + E + W + G;

        List<TradeInfo.TradeItemDetail> out = new ArrayList<>();

        // 3) 월세(세입자만). 음수 방지 + 하한 150,000
        if (tenure == FixedObligation.TenureType.RENTER_MONTHLY) {
            double wr = renterShare(decile); // 세입자 비중 하한
            int rentUnmix = Math.max(150_000, (int) Math.round((targetTotal - utilities) / wr)); // 언믹스
            double alphaAge = alphaByAge(ageGroup); // 연령대별 ‘실제주거비’ 비율(0.081, 0.031, ...)
            double ageAdj = Math.sqrt(Math.max(0.5, Math.min(2.0, alphaAge / ALPHA_REF))); // 완만 보정(과격 방지 클램프)
            int rentBase = roundK((int) Math.round(rentUnmix * ageAdj));

            out.add(range("월세", rentBase, 0.15)); // ±15%
        }

        // 4) 유틸 범위(성향별 변동폭 적용)
        double v = t.variationPct();
        out.add(range("관리비", M, v));
        out.add(range("전기 요금", E, v));
        out.add(range("수도 요금", W, v));
        out.add(range("도시가스 요금", G, v));

        return out;
    }

    private static TradeInfo.TradeItemDetail range(String name, int base, double pct) {
        int lo = roundK((int) Math.round(base * (1 - pct)));
        int hi = roundK((int) Math.round(base * (1 + pct)));
        return new TradeInfo.TradeItemDetail(name, lo, hi);
    }

    private static int roundK(int v) {
        return (v + 500) / 1000 * 1000;
    }

    // 성향별 간단 튜닝(월세 제외)
    private static UtilityTuning tuningOf(PreferenceType p) {
        return switch (p) {
            case CONSUMPTION_ORIENTED -> new UtilityTuning(1.05, 1.08, 1.05, 1.05, 0.18);
            case SAVING_ORIENTED -> new UtilityTuning(0.95, 0.92, 0.95, 0.95, 0.12);
            case UNPLANNED -> new UtilityTuning(1.02, 1.05, 1.03, 1.05, 0.25);
            case INVESTMENT_ORIENTED -> new UtilityTuning(0.98, 0.98, 1.00, 1.00, 0.15);
            case STABLE -> new UtilityTuning(1.00, 0.98, 0.98, 0.98, 0.10);
            default -> new UtilityTuning(1.00, 1.00, 1.00, 1.00, 0.15);
        };
    }

    /** 분위별 세입자 비중 w_r (튜닝 포인트)*/
    private static double renterShare(int decile) {
        return switch (decile) {
            case 1 -> 0.70;
            case 2 -> 0.65;
            case 3 -> 0.60;
            case 4 -> 0.55;
            case 5 -> 0.50;
            case 6 -> 0.45;
            case 7 -> 0.40;
            case 8 -> 0.35;
            case 9 -> 0.30;
            default -> 0.25;
        };
    }

    /**
     * 연령대별 ‘실제주거비’ 비율 α_age (입력 값 맵핑) - 통계청 통계 프리즘 자료 참고
     */
    private static double alphaByAge(int ageGroup) {
        if (ageGroup <= 30) return 0.081; // ≤35세
        if (ageGroup <= 40) return 0.031; // ≤45세
        if (ageGroup <= 50) return 0.034; // ≤55세
        if (ageGroup <= 60) return 0.032; // ≤65세
        return 0.038;                     // 66세+
    }

    private record UtilityTuning(double mMaint, double mElec, double mWater, double mGas, double variationPct) {
    }
}
