package com.simpaylog.generatorcore.strategy.impl;

import com.simpaylog.generatorcore.strategy.WageDateStrategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegularWageDateStrategy implements WageDateStrategy {

    private static final int PAYDAY = 25; // 월급일
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
        LocalDate scheduledPayDay = LocalDate.of(baseDate.getYear(), baseDate.getMonth(), PAYDAY);
        LocalDate adjusted = adjustPayDate(scheduledPayDay);
        return Collections.singletonList(adjusted);
    }

    public LocalDate adjustPayDate(LocalDate candidateDate) {
        while (isWeekend(candidateDate) || isHoliday(candidateDate)) {
            candidateDate = candidateDate.minusDays(1); // 당김
        }
        return candidateDate;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isHoliday(LocalDate date) {
        return HOLIDAYS.contains(date);
    }
}
