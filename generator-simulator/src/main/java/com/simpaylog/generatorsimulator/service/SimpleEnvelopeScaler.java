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

    public SimpleEnvelopeScaler(Map<CategoryType, BigDecimal> budget, Map<CategoryType, Integer> events, double lamdaMin, double lamdaMax) {
        this.remainingBudget = new EnumMap<>(CategoryType.class);
        this.remainingEvents = new EnumMap<>(CategoryType.class);
        this.lamdaMin = lamdaMin;
        this.lamdaMax = lamdaMax;

        this.remainingBudget.putAll(budget);
        for (Map.Entry<CategoryType, Integer> entry : events.entrySet()) {
            this.remainingEvents.put(entry.getKey(), Math.max(1, entry.getValue()));
        }
    }

    public SimpleEnvelopeScaler(Map<CategoryType, BigDecimal> envelope, Map<CategoryType, Integer> approxCounts) {
        this(envelope, approxCounts, 0.65, 1.75);
    }

    // 최종 금액 계산
    public BigDecimal computeScaledAmount(CategoryType category, BigDecimal sampleAmount) {
        BigDecimal remainingAmount = remainingBudget.getOrDefault(category, BigDecimal.ZERO);
        int remainCnt = remainingEvents.getOrDefault(category, 0);
        if (!checkCondition(sampleAmount, remainingAmount, remainCnt)) {
            return BigDecimal.ZERO;
        }

        // 목표 금액(샘플 금액이 목표보다 크면 금액을 줄이고, 작으면 금액을 늘림): 남은 예산 / 남은 이벤트 수
        BigDecimal targetAmount = remainingAmount.divide(BigDecimal.valueOf(remainCnt), 8, RoundingMode.HALF_UP);

        BigDecimal lambdaBD = targetAmount.divide(sampleAmount, 8, RoundingMode.HALF_UP);
        double lambda = clamp(lambdaBD.doubleValue(), lamdaMin, lamdaMax);
        // 최종 금액(반올림/10원 단위 정리)
        BigDecimal cost = sampleAmount.multiply(BigDecimal.valueOf(lambda)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal finalAmount = MoneyUtil.roundTo10(cost);
        return finalAmount;
    }

    public void applySpend(CategoryType category, BigDecimal appliedAmount) {
        if (appliedAmount == null || appliedAmount.signum() <= 0) return; // 0원은 적용하지 않음

        // 예산 차감 (마이너스 방지)
        BigDecimal remainingAmount = remainingBudget.getOrDefault(category, BigDecimal.ZERO);
        BigDecimal nextAmount = remainingAmount.subtract(appliedAmount);
        if (nextAmount.signum() < 0) nextAmount = BigDecimal.ZERO;
        remainingBudget.put(category, nextAmount);

        // 이벤트 카운트 1 감소 (마이너스 방지)
        int remainCnt = remainingEvents.getOrDefault(category, 0);
        if (remainCnt > 0) {
            remainingEvents.put(category, remainCnt - 1);
        }
    }

    public boolean checkCondition(BigDecimal sampleAmount, BigDecimal remainingAmount, int remainCnt) {
        // 상품 금액이 들어오지 않았거나 0원인 경우
        if (sampleAmount == null || sampleAmount.signum() <= 0) {
            return false;
        }
        // 남은 예산이 0원이거나 잔여 이벤트 수가 없을 경우
        if (remainingAmount.signum() <= 0 || remainCnt <= 0) {
            return false;
        }
        return true;
    }

    private double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }
}
