package com.simpaylog.generatorcore.enums.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionCsvExportHeader {
    TRANSACTION_ID("transactionId", "transaction_id", "거래ID"),
    USER_ID("userId", "user_id", "사용자ID"),
    TIMESTAMP("timestamp", "timestamp", "타임스탬프"),
    TRANSACTION_TYPE("transactionType", "transaction_type", "거래유형"),
    DETAIL_TYPE("detailType", "detail_type", "거래 상세유형"),
    CATEGORY("category", "category", "거래 대분류"),
    SUBCATEGORY("subcategory", "subcategory", "거래 소분류"),
    COUNTERPARTY("counterparty", "counterparty", "가맹점명"),
    CHANNEL("channel", "channel", "채널"),
    BALANCE_BEFORE("balanceBefore", "balance_before", "거래 전 잔액"),
    BALANCE_AFTER("balanceAfter", "balance_after", "거래 후 잔액"),
    DESCRIPTION("description", "description", "설명"),
    MEMO("memo", "memo", "메모"),
    AMOUNT("amount", "amount", "금액"),

    // 집계 컬럼 예시
    TOTAL_SPENT("totalSpent", "total_spent", "총 지출액"),
    AVG_TRANSACTION("avgTxn", "avg_transaction", "평균 거래액"),
    PERIOD("period", "period", "집계 기준 기간(년-월)"),
    INCOME_VS_SPENDING("incomeVsSpending", "income_vs_spending", "소득 대비 지출 비율(수입이 전체 거래에서 차지하는 비율)"),
    TOP_3_CATEGORIES("top3Categories", "top_3_categories", "최빈 카테고리 3개"),
    FOOD_RATIO("foodRatio", "food_ratio", "음식 카테고리 거래 비율"),
    TRANSPORT_RATIO("transportRatio", "transport_ratio", "교통 카테고리 거래 비율"),
    LEISURE_RATIO("leisureRatio", "leisure_ratio", "레저 카테고리 거래 비율");
//    AGG_USER_ID("aggUserId", "user_id", "레저 카테고리 거래 비율");

    private final String fieldName; // TransactionLogDocument 또는 집계 DTO 필드명
    private final String displayName;
    private final String description; // CSV 헤더
}
