package com.simpaylog.generatorcore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record DailyTransactionResult(
        String sessionId,
        Long userId,
        boolean success,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date
) {
}
