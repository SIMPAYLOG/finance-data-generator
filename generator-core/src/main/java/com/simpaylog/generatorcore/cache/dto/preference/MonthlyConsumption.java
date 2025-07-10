package com.simpaylog.generatorcore.cache.dto.preference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MonthlyConsumption(
    BigDecimal monthlyTotalConsumption,
    BigDecimal monthlyTotalSurplus,
    List<DailyConsumption> DailyConsumptionList
) {
    public record DailyConsumption (
    LocalDate date,
    BigDecimal groceriesNonAlcoholicBeverages,
    BigDecimal alcoholicBeveragesTobacco,
    BigDecimal clothingFootwear,
    BigDecimal housingUtilitiesFuel,
    BigDecimal householdGoodsServices,
    BigDecimal health,
    BigDecimal transportation,
    BigDecimal communication,
    BigDecimal recreationCulture,
    BigDecimal education,
    BigDecimal foodAccommodation,
    BigDecimal otherGoodsServices,
    BigDecimal dailyTotalConsumption
) {}
}
