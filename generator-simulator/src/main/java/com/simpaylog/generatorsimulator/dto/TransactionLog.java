package com.simpaylog.generatorsimulator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionLog(
        String uuid,
        String sessionId,
        Long userId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm")
        LocalDateTime timestamp,
        TransactionType transactionType,
        String description,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter
) {
    public enum TransactionType {
        WITHDRAW,
        DEPOSIT
    }

    public static TransactionLog of(Long userId, String sessionId, LocalDateTime timestamp, TransactionType transactionType,String description, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return new TransactionLog(
                UUID.randomUUID().toString(),
                sessionId,
                userId,
                timestamp,
                transactionType,
                description,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

}
