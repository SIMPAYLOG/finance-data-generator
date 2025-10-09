package com.simpaylog.generatorcore.dto;

import java.util.List;

public record TransactionResult(
        boolean success,
        String reason,
        List<TransactionLog> logs
) {

    public static TransactionResult ok(TransactionLog log) {
        return new TransactionResult(true, null, List.of(log));
    }
    public static TransactionResult ok(List<TransactionLog> logs) {
        return new TransactionResult(true, null, logs);
    }
    public static TransactionResult fail(String reason) {
        return new TransactionResult(false, reason, List.of());
    }
}
