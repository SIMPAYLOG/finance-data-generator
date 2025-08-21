package com.simpaylog.generatorcore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.simpaylog.generatorcore.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionLog(
        String uuid,
        String sessionId,
        Long userId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        TransactionType transactionType,
        String description,
        BigDecimal amount
) {


    public static TransactionLog of(Long userId, String sessionId, LocalDateTime timestamp, TransactionType transactionType,String description, BigDecimal amount) {
        return new TransactionLog(
                UUID.randomUUID().toString(),
                sessionId,
                userId,
                timestamp,
                transactionType,
                description,
                amount
        );
    }

}
