package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorcore.utils.MoneyUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

public final class SimpleEnvelopeScaler {
    private final EnumMap<CategoryType, BigDecimal> remainingBudget;
    private final EnumMap<CategoryType, Integer> remainingEvents;

    private final double lamdaMin;
    private final double lamdaMax;

    public SimpleEnvelopeScaler(Map<CategoryType, BigDecimal> envelope, Map<CategoryType, Integer> approxCounts) {
        this(envelope, approxCounts, 0.65, 1.75);
    }

    public SimpleEnvelopeScaler(Map<CategoryType, BigDecimal> budget, Map<CategoryType, Integer> events, double lamdaMin, double lamdaMax) {
        this.remainingBudget = new EnumMap<>(CategoryType.class);
        this.remainingEvents = new EnumMap<>(CategoryType.class);
        this.lamdaMin = lamdaMin;
        this.lamdaMax = lamdaMax;

        this.remainingBudget.putAll(budget);
        for(Map.Entry<CategoryType, Integer> entry : events.entrySet()) {
            this.remainingEvents.put(entry.getKey(), Math.max(1, entry.getValue()));
        }
    }

    public BigDecimal scale(CategoryType category, BigDecimal sampleAmount) {
        if(sampleAmount == null || sampleAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = remainingBudget.getOrDefault(category, BigDecimal.ZERO); // 남은 예산
        int remainCnt = Math.max(1, remainingEvents.getOrDefault(category, 1));

        if(remainingAmount.signum() <= 0) { // 남은 예산이 0원인 경우
            remainingEvents.put(category, Math.max(0, remainCnt - 1));
            return BigDecimal.ZERO;
        }
        // 목표 금액(샘플 금액이 목표보다 크면 금액을 줄이고, 작으면 금액을 늘림)
        BigDecimal targetAmount = remainingAmount.divide(BigDecimal.valueOf(remainCnt), 8, RoundingMode.HALF_UP);
        BigDecimal lambdaBD = targetAmount.divide(sampleAmount, 8, RoundingMode.HALF_UP);
        double lambda = clamp(lambdaBD.doubleValue(), lamdaMin, lamdaMax);

        // 최종 금액
        BigDecimal cost = sampleAmount.multiply(BigDecimal.valueOf(lambda)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal finalAmount = MoneyUtil.roundTo10(cost);
        BigDecimal nextAmount = remainingAmount.subtract(finalAmount);
        if(nextAmount.signum() < 0) nextAmount = BigDecimal.ZERO;
        remainingBudget.put(category, nextAmount);
        remainingEvents.put(category, Math.max(0, remainCnt - 1));

        return finalAmount;
    }

    public void rollback(CategoryType category, BigDecimal committedFinalAmount) {
        remainingBudget.merge(category, committedFinalAmount, BigDecimal::add);
        remainingEvents.merge(category, 1, Integer::sum);
    }

    public BigDecimal getRemainingBudget(CategoryType category) {
        return remainingBudget.getOrDefault(category, BigDecimal.ZERO);
    }

    public int getRemainingEvents(CategoryType category) {
        return remainingEvents.getOrDefault(category, 0);
    }

    private double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }
}
