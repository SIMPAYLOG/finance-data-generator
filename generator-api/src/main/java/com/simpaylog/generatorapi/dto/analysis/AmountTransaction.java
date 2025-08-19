package com.simpaylog.generatorapi.dto.analysis;

import java.util.List;

public record AmountTransaction(
        List<AmountAvgTransactionSummary> results
){
    public record AmountAvgTransactionSummary(
            String transactionType,
            int amount
    ) {}
}
