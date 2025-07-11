package com.simpaylog.generatorsimulator.dto;

public record CategorySpendingWeight(
        double groceriesNonAlcoholicBeverages,
        double alcoholicBeveragesTobacco,
        double clothingFootwear,
        double housingUtilitiesFuel,
        double householdGoodsServices,
        double health,
        double transportation,
        double communication,
        double recreationCulture,
        double education,
        double foodAccommodation,
        double otherGoodsServices
) {
}
