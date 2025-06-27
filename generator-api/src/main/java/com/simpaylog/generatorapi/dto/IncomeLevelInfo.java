package com.simpaylog.generatorapi.dto;

import java.math.BigDecimal;

public record IncomeLevelInfo(
        int decile,
        AssetRange assetRange,
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
        BigDecimal consumption_expenditure,
        BigDecimal nonConsumption_expenditure,
        BigDecimal surplusRatePct,
        BigDecimal avgPropensityToConsumePct
) {
    public record AssetRange(int min, int max) {
    }
}