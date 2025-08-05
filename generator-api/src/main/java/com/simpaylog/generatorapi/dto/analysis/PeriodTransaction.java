package com.simpaylog.generatorapi.dto.analysis;

import java.util.List;

public record PeriodTransaction(
        AggregationInterval interval,
        List<PTSummary> results
) {

    public record PTSummary(
            String key,
            int totalSpentCount,
            double spentAmountSum,
            int totalIncomeCount,
            double incomeAmountSum
    ) {

    }
}
