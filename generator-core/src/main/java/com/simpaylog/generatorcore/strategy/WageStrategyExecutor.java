package com.simpaylog.generatorcore.strategy;

import com.simpaylog.generatorcore.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class WageStrategyExecutor {

    public List<LocalDate> decidePayout(User user, LocalDate baseDate) {
        return user.getUserBehaviorProfile().getWageType().getStrategy().getPayOutDates(baseDate);
    }
}
