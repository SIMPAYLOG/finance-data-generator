package com.simpaylog.generatorsimulator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class MonthlyConsumptionCost {
    private List<DailyConsumptionCost> dailyConsumptionCostList;
    private Long totalConsumptionCost;
    private Long totalSurplusCost;
}
