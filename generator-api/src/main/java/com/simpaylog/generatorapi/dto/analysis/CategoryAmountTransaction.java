package com.simpaylog.generatorapi.dto.analysis;

import java.util.List;

public record CategoryAmountTransaction(
        List<CategoryAmountTransaction.AmountTransactionSummary> results
){
    public record AmountTransactionSummary(
            String categoryType,
            int amount
    ) {}
}