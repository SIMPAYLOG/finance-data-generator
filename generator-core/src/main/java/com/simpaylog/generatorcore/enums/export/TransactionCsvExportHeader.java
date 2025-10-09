package com.simpaylog.generatorcore.enums.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionCsvExportHeader {
    TRANSACTION_ID("transactionId", "거래ID"),
    USER_ID("userId", "사용자ID"),
    TIMESTAMP("timestamp", "타임스탬프"),
    TRANSACTION_TYPE("transactionType", "거래유형"),
    DETAIL_TYPE("detailType", "거래 상세유형"),
    CATEGORY("category", "거래 대분류"),
    SUBCATEGORY("subcategory", "거래 소분류"),
    COUNTERPARTY("counterparty", "가맹점명"),
    CHANNEL("channel", "채널"),
    BALANCE_BEFORE("balanceBefore", "거래 전 잔액"),
    BALANCE_AFTER("balanceAfter", "거래 후 잔액"),
    DESCRIPTION("description", "설명"),
    MEMO("memo", "메모"),
    AMOUNT("amount", "금액"),

    // 집계 컬럼 예시
    TOTAL_SPENT("totalSpent", "총 지출액"),
    AVG_TRANSACTION("avgTransaction", "평균 거래액"),
    CATEGORY_RATIOS("categoryRatios", "카테고리별 비율"),
    INCOME_VS_SPENDING("incomeVsSpending", "소득 대비 지출 비율");

    private final String fieldName; // TransactionLogDocument 또는 집계 DTO 필드명
    private final String displayName; // CSV 헤더
}
