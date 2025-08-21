package com.simpaylog.generatorsimulator.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class TransactionSegmentsUtilTest {

    @Test
    void 경계테스트_걸쳐있는_기간을_줬을때_2개의_MonthSegment를_가진_리스트를_생성() {
        // Given
        LocalDate from = LocalDate.of(2025, 8, 29);
        LocalDate to = LocalDate.of(2025, 9, 2);

        // When
        var segs = TransactionSegmentsUtil.splitByMonth(from, to);

        // Then
        assertEquals(2, segs.size());
        assertEquals(YearMonth.of(2025, 8), segs.getFirst().ym());
        assertEquals(LocalDate.of(2025, 8, 29), segs.getFirst().start());
        assertEquals(LocalDate.of(2025, 8, 31), segs.getFirst().end());
        assertEquals(YearMonth.of(2025, 9), segs.getLast().ym());
        assertEquals(LocalDate.of(2025, 9, 1), segs.getLast().start());
        assertEquals(LocalDate.of(2025, 9, 2), segs.getLast().end());
    }

}