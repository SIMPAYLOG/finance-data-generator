package com.simpaylog.generatorcore.cache.dto;

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
        BigDecimal consumptionExpenditure,
        BigDecimal nonConsumptionExpenditure,
        BigDecimal surplusRatePct,
        BigDecimal avgPropensityToConsumePct
) {
    public record AssetRange(int min, int max) {
    }
}