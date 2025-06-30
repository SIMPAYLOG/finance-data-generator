package com.simpaylog.generatorcore.strategy.impl;

import com.simpaylog.generatorcore.strategy.WageDateStrategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BiWeeklyWageDateStrategy implements WageDateStrategy {

    private static final Set<LocalDate> HOLIDAYS = new HashSet<>(Set.of(
            LocalDate.of(2025, 1, 1),  // 신정
            LocalDate.of(2025, 3, 1),  // 삼일절
            LocalDate.of(2025, 5, 5),  // 어린이날
            LocalDate.of(2025, 6, 6),  // 현충일
            LocalDate.of(2025, 8, 15), // 광복절
            LocalDate.of(2025, 10, 3), // 개천절
            LocalDate.of(2025, 10, 9), // 한글날
            LocalDate.of(2025, 12, 25) // 성탄절
    ));

    @Override
    public List<LocalDate> getPayOutDates(LocalDate baseDate) {
        List<LocalDate> result = new ArrayList<>();
        LocalDate firstDay = baseDate.withDayOfMonth(1);
        LocalDate lastDay = baseDate.withDayOfMonth(baseDate.lengthOfMonth());

        // 첫 번째 금요일 찾기
        LocalDate firstPay = firstDay.with(DayOfWeek.FRIDAY);
        if (firstPay.isBefore(firstDay)) {
            firstPay = firstPay.plusWeeks(1);
        }

        // 14일 간격으로 지급일 추가
        while (!firstPay.isAfter(lastDay)) {
            result.add(adjustPayDate(firstPay));
            firstPay = firstPay.plusDays(14);
        }

        return result;
    }

    public LocalDate adjustPayDate(LocalDate candidateDate) {
        while (isWeekend(candidateDate) || isHoliday(candidateDate)) {
            candidateDate = candidateDate.minusDays(1); // 당김
        }
        return candidateDate;
    }

    private boolean isWeekend(LocalDate candidateDate) {
        DayOfWeek day = candidateDate.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isHoliday(LocalDate date) {
        return HOLIDAYS.contains(date);
    }
}
