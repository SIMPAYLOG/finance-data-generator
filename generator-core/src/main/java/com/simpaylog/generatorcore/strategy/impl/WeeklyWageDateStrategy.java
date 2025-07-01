package com.simpaylog.generatorcore.strategy.impl;

import com.simpaylog.generatorcore.strategy.WageDateStrategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WeeklyWageDateStrategy implements WageDateStrategy {

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

        // 1일부터 말일까지 순회하며 매주 지급일 설정
        LocalDate date = firstDay.with(DayOfWeek.FRIDAY);
        if (date.isBefore(firstDay)) {
            date = date.plusWeeks(1); // 1일 이전이면 다음 주 금요일부터 시작
        }

        while (!date.isAfter(lastDay)) {
            // 주말 또는 공휴일이면 하루 앞당김 (예: 금 → 목)
            LocalDate adjusted = adjustPayDate(date);
            result.add(adjusted);
            date = date.plusWeeks(1);
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