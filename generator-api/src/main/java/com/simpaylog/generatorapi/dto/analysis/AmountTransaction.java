package com.simpaylog.generatorapi.dto.analysis;

import java.util.List;

public record AmountTransaction(
        List<AmountTransactionSummary> results
){
    public record AmountTransactionSummary(
            String transactionType,
            int amount
    ) {}
}
