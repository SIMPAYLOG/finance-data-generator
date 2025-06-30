package com.simpaylog.generatorcore.strategy;

import java.time.LocalDate;
import java.util.List;

public interface WageDateStrategy {
    List<LocalDate> getPayOutDates(LocalDate baseDate);
}
