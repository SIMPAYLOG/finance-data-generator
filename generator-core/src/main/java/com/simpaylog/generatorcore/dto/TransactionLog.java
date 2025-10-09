package com.simpaylog.generatorcore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.simpaylog.generatorcore.enums.TransactionDetailType;
import com.simpaylog.generatorcore.enums.TransactionType;
import com.simpaylog.generatorcore.enums.ChannelType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionLog(

        String transactionId,                        // 거래 고유 ID
        Long userId,                        // 사용자 ID
        String sessionId,                   // 시뮬레이션 세션 ID

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,             // 거래 발생 시각

        TransactionType transactionType,     // 입금-DEPOSIT, 출금-WITHDRAW
        TransactionDetailType detailType,    // 거래 유형 (예: CARD_PAYMENT, DEPOSIT 등)
        ChannelType channel,                 // 거래 채널 (CARD, ATM, MOBILE 등)

        String description,                  // 거래 설명 (예: 스타벅스 결제)
        String counterparty,                 // 상대 정보 (가맹점명, 계좌이체 대상 등)
        String memo,                         // 메모/비고 (선택 필드)

        BigDecimal amount,                   // 거래 금액
        BigDecimal balanceBefore,            // 거래 전 잔액
        BigDecimal balanceAfter              // 거래 후 잔액
) {

    // ---- 정적 생성 메서드 ----
    public static TransactionLog of(
            Long userId,
            String sessionId,
            LocalDateTime timestamp,
            TransactionType transactionType,
            TransactionDetailType detailType,
            String description,
            String counterparty,
            String memo,
            BigDecimal amount,
            BigDecimal balanceBefore
    ) {
        ChannelType channel = detailType.getChannel();

        // 거래 방향에 따라 잔액 계산
        BigDecimal balanceAfter = switch (transactionType) {
            case DEPOSIT -> balanceBefore.add(amount);
            case WITHDRAW -> balanceBefore.subtract(amount);
        };

        return new TransactionLog(
                UUID.randomUUID().toString(),
                userId,
                sessionId,
                timestamp,
                transactionType,
                detailType,
                channel,
                description,
                counterparty,
                memo,
                amount,
                balanceBefore,
                balanceAfter
        );
    }
}
