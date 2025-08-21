package com.simpaylog.generatorapi.dto.analysis;

import java.math.BigDecimal;

public record TransactionHistoryDataDto(
        String timestamp,
        String category,
        String description,
        BigDecimal amount,
        String transactionType
) {
}
