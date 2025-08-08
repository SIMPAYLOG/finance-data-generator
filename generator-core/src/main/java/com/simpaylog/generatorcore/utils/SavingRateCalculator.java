package com.simpaylog.generatorcore.utils;

import com.simpaylog.generatorcore.enums.PreferenceType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;

public class SavingRateCalculator {
    private static final Random random = new Random();
    private static final Map<Integer, double[]> decileSavingRanges = Map.of(
            1, new double[]{0.00, 0.02},
            2, new double[]{0.01, 0.04},
            3, new double[]{0.03, 0.07},
            4, new double[]{0.05, 0.10},
            5, new double[]{0.05, 0.10},
            6, new double[]{0.05, 0.10},
            7, new double[]{0.10, 0.17},
            8, new double[]{0.12, 0.18},
            9, new double[]{0.15, 0.22},
            10, new double[]{0.20, 0.30}
    );
    private static final Map<PreferenceType, Double> preferenceBiasMap = Map.of(
            PreferenceType.SAVING_ORIENTED, 0.05,
            PreferenceType.CONSUMPTION_ORIENTED, -0.05,
            PreferenceType.UNPLANNED, -0.03,
            PreferenceType.INVESTMENT_ORIENTED, 0.00,
            PreferenceType.STABLE, 0.02
    );

    public static BigDecimal calculateSavingRate(int decile, int age, PreferenceType preferenceType) {
        double[] range = decileSavingRanges.getOrDefault(decile, new double[]{0.05, 0.10}); // 분위별 저축률(최소, 최대)
        double base = randomDouble(range[0], range[1]);

        double bias = preferenceBiasMap.getOrDefault(preferenceType, 0.0); // 성향 보정
        double ageBias = getAgeBias(age); // 나이 보정

        double finalRate = base + bias + ageBias;
        return BigDecimal.valueOf(clamp(finalRate, 0.0, 0.5)).setScale(2, RoundingMode.HALF_UP);
    }

    private static double getAgeBias(int age) {
        if (age < 30) return -0.01;
        if (age < 40) return 0.00;
        if (age < 50) return 0.01;
        return 0.02;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}
