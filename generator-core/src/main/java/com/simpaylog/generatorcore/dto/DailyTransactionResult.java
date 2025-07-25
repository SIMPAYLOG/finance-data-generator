package com.simpaylog.generatorcore.dto;

import java.time.LocalDate;

public record DailyTransactionResult(
        String sessionId,
        Long userId,
        boolean success,
        LocalDate date
) {
}
