package com.simpaylog.generatorapi.dto.analysis;

public record IncomeExpenseDto(
        long totalIncome,
        long totalExpense,
        long savings
) {}