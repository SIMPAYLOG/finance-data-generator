package com.simpaylog.generatorsimulator.dto;

import java.time.LocalDate;
import java.util.Map;

public record DailyTransactionResult(
        Long userId,
        LocalDate date,
        Integer transactionCount,
        Long totalSpending,
        Map<CategoryType, Long> spendingByCategory,
        boolean success,
        String errorMessage
) {

    public static DailyTransactionResult success(Long userId, LocalDate date, int transactionCount, long totalSpending, Map<CategoryType, Long> spendingByCategory) {
        return new DailyTransactionResult(userId, date, transactionCount, totalSpending, spendingByCategory, true, null);
    }

    public static DailyTransactionResult fail(Long userId, LocalDate date, int transactionCount, long totalSpending, Map<CategoryType, Long> spendingByCategory, String errorMessage) {
        return new DailyTransactionResult(userId, date, transactionCount, totalSpending, spendingByCategory, false, errorMessage);
    }
}
