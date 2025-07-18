package com.simpaylog.generatorsimulator.dto;

import java.time.LocalDate;
import java.util.Map;

public record DailyTransactionResult(
        Long userId,
        LocalDate date,
        Integer spendingTransactionCount,
        Long totalSpending,
        Map<CategoryType, Long> spendingByCategory,
        Integer incomeTransactionCount,
        Long totalIncome,
        boolean success,
        String errorMessage
) {

    public static DailyTransactionResult success(Long userId, LocalDate date, int spendingTransactionCount, long totalSpending, Map<CategoryType, Long> spendingByCategory, int incomeTransactionCount, long totalIncome) {
        return new DailyTransactionResult(userId, date, spendingTransactionCount, totalSpending, spendingByCategory, incomeTransactionCount, totalIncome, true, null);
    }

    public static DailyTransactionResult fail(Long userId, LocalDate date, int spendingTransactionCount, long totalSpending, Map<CategoryType, Long> spendingByCategory, int incomeTransactionCount, long totalIncome, String errorMessage) {
        return new DailyTransactionResult(userId, date, spendingTransactionCount, totalSpending, spendingByCategory, incomeTransactionCount, totalIncome, false, errorMessage);
    }
}
