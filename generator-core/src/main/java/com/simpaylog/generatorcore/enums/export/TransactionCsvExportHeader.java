package com.simpaylog.generatorcore.enums.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionCsvExportHeader {
    UUID("UUID"),
    USER_ID("사용자ID"),
    TIMESTAMP("타임스탬프"),
    TRANSACTION_TYPE("거래유형"),
    DESCRIPTION("설명"),
    AMOUNT("금액");

    private final String displayName;

}
