package com.simpaylog.generatorcore.strategy.impl;

import com.simpaylog.generatorcore.strategy.WageDateStrategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyWageDateStrategy implements WageDateStrategy {

    @Override
    public List<LocalDate> getPayOutDates(LocalDate baseDate) {
        List<LocalDate> result = new ArrayList<>();
        LocalDate firstDay = baseDate.withDayOfMonth(1);
        LocalDate lastDay = baseDate.withDayOfMonth(baseDate.lengthOfMonth());

        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            if (isWeekday(date)) {
                result.add(date);
            }
        }

        return result;
    }

    private boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}
