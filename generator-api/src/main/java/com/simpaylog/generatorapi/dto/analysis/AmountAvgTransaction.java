package com.simpaylog.generatorapi.dto.analysis;

import java.util.List;

public record AmountAvgTransaction(
        List<AmountAvgTransactionSummary> results
){
    public record AmountAvgTransactionSummary(
            String transactionType,
            int avgAmount
    ) {}
}
