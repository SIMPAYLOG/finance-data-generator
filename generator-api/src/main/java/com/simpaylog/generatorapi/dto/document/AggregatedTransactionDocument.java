package com.simpaylog.generatorapi.dto.document;

import java.math.BigDecimal;
import java.util.List;

public record AggregatedTransactionDocument(
        String userId,
        String period, // yyyy-MM
        BigDecimal totalSpent,
        BigDecimal avgTxn,
        List<String> top3Categories,
        Double foodRatio,
        Double transportRatio,
        Double leisureRatio,
        Double incomeVsSpending
) { }
