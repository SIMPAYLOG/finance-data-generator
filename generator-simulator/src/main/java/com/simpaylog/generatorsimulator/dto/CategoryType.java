package com.simpaylog.generatorsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryType {
    GROCERIES_NON_ALCOHOLIC_BEVERAGES("groceriesNonAlcoholicBeverages", "식료품 및 비주류 음료"),
    ALCOHOLIC_BEVERAGES_TOBACCO("alcoholicBeveragesTobacco", "주류 및 담배"),
    CLOTHING_FOOTWEAR("clothingFootwear", "의류 및 신발"),
    HOUSING_UTILITIES_FUEL("housingUtilitiesFuel", "주거 및 공공요금"),
    HOUSEHOLD_GOODS_SERVICES("householdGoodsServices", "가정용품 및 서비스"),
    HEALTH("health", "보건/의료"),
    TRANSPORTATION("transportation", "교통"),
    COMMUNICATION("communication", "통신"),
    RECREATION_CULTURE("recreationCulture", "오락 및 문화"),
    EDUCATION("education", "교육"),
    FOOD_ACCOMMODATION("foodAccommodation", "외식 및 숙박"),
    OTHER_GOODS_SERVICES("otherGoodsServices", "기타 상품 및 서비스");

    private final String key;
    private final String label;

    public static CategoryType fromKey(String key) {
        return switch (key) {
            case "groceriesNonAlcoholicBeverages" -> GROCERIES_NON_ALCOHOLIC_BEVERAGES;
            case "alcoholicBeveragesTobacco" -> ALCOHOLIC_BEVERAGES_TOBACCO;
            case "clothingFootwear" -> CLOTHING_FOOTWEAR;
            case "housingUtilitiesFuel" -> HOUSING_UTILITIES_FUEL;
            case "householdGoodsServices" -> HOUSEHOLD_GOODS_SERVICES;
            case "health" -> HEALTH;
            case "transportation" -> TRANSPORTATION;
            case "communication" -> COMMUNICATION;
            case "recreationCulture" -> RECREATION_CULTURE;
            case "education" -> EDUCATION;
            case "foodAccommodation" -> FOOD_ACCOMMODATION;
            case "otherGoodsServices" -> OTHER_GOODS_SERVICES;
            case null, default -> null;
        };
    }
}
