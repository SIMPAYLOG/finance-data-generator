package com.simpaylog.generatorcore.enums;

import com.simpaylog.generatorcore.strategy.*;
import com.simpaylog.generatorcore.strategy.impl.*;

public enum WageType {
    DAILY,
    WEEKLY,
    BI_WEEKLY,
    REGULAR,
    RANDOM;

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
