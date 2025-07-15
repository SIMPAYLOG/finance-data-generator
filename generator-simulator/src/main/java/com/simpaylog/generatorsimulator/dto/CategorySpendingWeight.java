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
    public double getValueByCategoryType(CategoryType categoryType) {
        return switch (categoryType) {
            case GROCERIES_NON_ALCOHOLIC_BEVERAGES -> groceriesNonAlcoholicBeverages;
            case ALCOHOLIC_BEVERAGES_TOBACCO -> alcoholicBeveragesTobacco;
            case CLOTHING_FOOTWEAR ->  clothingFootwear;
            case HOUSING_UTILITIES_FUEL ->  housingUtilitiesFuel;
            case HOUSEHOLD_GOODS_SERVICES ->  householdGoodsServices;
            case HEALTH ->  health;
            case TRANSPORTATION -> transportation;
            case COMMUNICATION ->  communication;
            case RECREATION_CULTURE -> recreationCulture;
            case EDUCATION -> education;
            case FOOD_ACCOMMODATION -> foodAccommodation;
            case OTHER_GOODS_SERVICES -> otherGoodsServices;
            case null, default -> 0;
        };
    }
}
