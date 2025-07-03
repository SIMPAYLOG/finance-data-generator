package com.simpaylog.generatorcore.cache.dto.preference;

import java.util.List;

public record MonthlyConsumptionCost (
    Long totalConsumptionCost,
    Long totalSurplusCost,
    List<DailyConsumptionCost> dailyConsumptionCostList
) {
    public record DailyConsumptionCost (
    String date,
    Long groceriesNonAlcoholicBeverages,
    Long alcoholicBeveragesTobacco,
    Long clothingFootwear,
    Long housingUtilitiesFuel,
    Long householdGoodsServices,
    Long health,
    Long transportation,
    Long communication,
    Long recreationCulture,
    Long education,
    Long foodAccommodation,
    Long otherGoodsServices
) {}
}
