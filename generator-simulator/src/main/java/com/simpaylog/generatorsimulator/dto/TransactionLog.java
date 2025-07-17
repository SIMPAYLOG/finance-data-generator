package com.simpaylog.generatorsimulator.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionLog(
        String uuid,
        Long userId,
        LocalDateTime timestamp,
        String transactionType,
        String description,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter
) {
    // 임금
    public static TransactionLog withdraw(Long userId, LocalDateTime timestamp, String description, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return new TransactionLog(
                UUID.randomUUID().toString(),
                userId,
                timestamp,
                "WITHDRAW",
                description,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

    // 출금
    public static TransactionLog deposit(Long userId, LocalDateTime timestamp, String description, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return new TransactionLog(
                UUID.randomUUID().toString(),
                userId,
                timestamp,
                "DEPOSIT",
                description,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

}
