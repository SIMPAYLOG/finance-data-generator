package com.simpaylog.generatorapi.dto.analysis;

import java.util.List;

public record HourlyTransaction(
        List<HourlySummary> results
) {
    public record HourlySummary(
            int hour,                  // 시간 (0~23)
            int totalSpentCount,      // WITHDRAW 거래 수
            int avgSpentAmount,    // WITHDRAW 평균 금액
            int totalIncomeCount,     // DEPOSIT 거래 수
            int avgIncomeAmount    // DEPOSIT 평균 금액
    ) {}
}