package com.simpaylog.generatorsimulator.dto;

public record ConsumptionDelta (
    int totalDelta,
    int groceriesNonAlcoholicBeverages,
    int alcoholicBeveragesTobacco,
    int clothingFootwear,
    int housingUtilitiesFuel,
    int householdGoodsServices,
    int health,
    int transportation,
    int communication,
    int recreationCulture,
    int education,
    int foodAccommodation,
    int otherGoodsServices
) {}