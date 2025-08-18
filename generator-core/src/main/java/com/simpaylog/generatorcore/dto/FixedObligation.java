package com.simpaylog.generatorcore.dto;

import com.simpaylog.generatorcore.enums.TransactionType;

import java.math.BigDecimal;
import java.time.DayOfWeek;

public record FixedObligation(
        Long userId,
        String categoryType,
        TransactionType transactionType,
        BigDecimal amount,
        IntervalRecurrence recurrence,
        TenureType tenureType,
        String description,
        String effectiveFrom,           // "yyyy-MM-dd" (포함)
        String effectiveTo             // nullable
) {

    public FixedObligation {
        if (userId == null) throw new IllegalArgumentException("userId");
        if (categoryType == null) throw new IllegalArgumentException("categoryType");
        if (transactionType == null) throw new IllegalArgumentException("transactionType");
        if (amount == null || amount.signum() < 0) throw new IllegalArgumentException("amount >= 0");
        if (recurrence == null) throw new IllegalArgumentException("recurrence");
        if (effectiveFrom == null || !effectiveFrom.matches("^\\d{4}-\\d{2}-\\d{2}$"))
            throw new IllegalArgumentException("effectiveFrom yyyy-MM-dd");
        if (effectiveTo != null && !effectiveTo.matches("^\\d{4}-\\d{2}-\\d{2}$"))
            throw new IllegalArgumentException("effectiveTo yyyy-MM-dd");
    }

    public record IntervalRecurrence(
            TimeUnit unit,          // DAYS, WEEKS, MONTHS, QUARTERS, YEARS
            int interval,           // >=1 (예: 격주는 2)
            DayOfWeek dayOfWeek,    // unit=WEEKS에서 사용 (그 외 null)
            Integer dayOfMonth,     // unit=MONTHS/QUARTERS/YEARS에서 사용 (그 외 null)
            Boolean lastDayOfMonth  // true면 항상 말일 (dayOfMonth 무시), 그 외 null/false
    ) {
    }

    public enum TenureType {
        RENTER_MONTHLY, // 월세
        RENTER_JEONSE, // 전세
        OWNER_MORTGAGE, // 자가(대출)
        OWNER_FULL // 자가(완납)
    }

    public enum TimeUnit {
        DAYS, WEEKS, MONTHS, QUARTERS, YEARS
    }
}
