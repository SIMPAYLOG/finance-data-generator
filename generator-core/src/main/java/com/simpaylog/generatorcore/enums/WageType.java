package com.simpaylog.generatorcore.enums;

import com.simpaylog.generatorcore.strategy.*;
import com.simpaylog.generatorcore.strategy.impl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WageType {
    DAILY(0.15),
    WEEKLY(0.05),
    BI_WEEKLY(0.1),
    REGULAR(0.1),
    RANDOM(0.2);

    private final double volatility;

    public WageDateStrategy getStrategy() {
        return switch (this) {
            case DAILY -> new DailyWageDateStrategy();
            case WEEKLY -> new WeeklyWageDateStrategy();
            case BI_WEEKLY -> new BiWeeklyWageDateStrategy();
            case REGULAR -> new RegularWageDateStrategy();
            case RANDOM -> new RandomWageDateStrategy();
        };
    }
}
