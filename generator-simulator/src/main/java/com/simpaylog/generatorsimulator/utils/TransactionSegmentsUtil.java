package com.simpaylog.generatorsimulator.utils;

import com.simpaylog.generatorcore.dto.CategoryType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class TransactionSegmentsUtil {
    private static final int INTERNAL_SCALE = 8;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    public record MonthSegment(YearMonth ym, LocalDate start, LocalDate end, double factor) {
    }

    public static List<MonthSegment> splitByMonth(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) return List.of();

        List<MonthSegment> segs = new ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            YearMonth ym = YearMonth.from(cursor);
            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd = ym.atEndOfMonth();

            LocalDate segStart = cursor.isAfter(monthStart) ? cursor : monthStart; // 둘 중에 늦은 날짜
            LocalDate segEnd = to.isBefore(monthEnd) ? to : monthEnd; // 둘 중에 빠른 날짜

            int daysInMonth = ym.lengthOfMonth();
            int days = Math.toIntExact(ChronoUnit.DAYS.between(segStart, segEnd) + 1);
            double factor = (double) days / (double) daysInMonth; // 기간 비율
            segs.add(new MonthSegment(ym, segStart, segEnd, factor));
            cursor = segEnd.plusDays(1); // 다음달로
        }
        return segs;
    }

    public static Map<CategoryType, BigDecimal> scaleBudget(Map<CategoryType, BigDecimal> monthlyBudget, double factor) {
        Map<CategoryType, BigDecimal> out = new EnumMap<>(CategoryType.class);
        BigDecimal f = BigDecimal.valueOf(factor);
        monthlyBudget.forEach((k, v) -> out.put(k, v.multiply(f).setScale(INTERNAL_SCALE, RM))); // 한달 예산 factor 만큼 나누기
        return out;
    }
}
