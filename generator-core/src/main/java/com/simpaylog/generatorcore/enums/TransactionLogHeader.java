package com.simpaylog.generatorcore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionLogHeader {
    UUID("UUID"),
    USER_ID("사용자ID"),
    TIMESTAMP("타임스탬프"),
    TRANSACTION_TYPE("거래유형"),
    DESCRIPTION("설명"),
    AMOUNT("금액"),
    BALANCE_BEFORE("거래 전 잔액"),
    BALANCE_AFTER("거래 후 잔액");

    private final String displayName;

}
