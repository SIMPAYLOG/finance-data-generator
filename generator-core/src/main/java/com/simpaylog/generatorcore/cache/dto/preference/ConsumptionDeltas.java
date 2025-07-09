package com.simpaylog.generatorcore.cache.dto.preference;

import java.math.BigDecimal;

public record ConsumptionDeltas(
    BigDecimal totalDelta, //실제 변화량 표기(100% -> 125%가 됐다면 해당 값은 25)
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
    BigDecimal otherGoodsServices
) {}