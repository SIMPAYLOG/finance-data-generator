package com.simpaylog.generatorsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryType {
    GROCERIES_NON_ALCOHOLIC_BEVERAGES("groceriesNonAlcoholicBeverages", "식료품 및 비주류 음료", 180),
    ALCOHOLIC_BEVERAGES_TOBACCO("alcoholicBeveragesTobacco", "주류 및 담배", 720),
    CLOTHING_FOOTWEAR("clothingFootwear", "의류 및 신발", 1440),
    HOUSING_UTILITIES_FUEL("housingUtilitiesFuel", "주거 및 공공요금", 2880),
    HOUSEHOLD_GOODS_SERVICES("householdGoodsServices", "가정용품 및 서비스", 720),
    HEALTH("health", "보건/의료", 720),
    TRANSPORTATION("transportation", "교통", 30),
    COMMUNICATION("communication", "통신", 1440),
    RECREATION_CULTURE("recreationCulture", "오락 및 문화", 480),
    EDUCATION("education", "교육", 1440),
    FOOD_ACCOMMODATION("foodAccommodation", "외식 및 숙박", 180),
    OTHER_GOODS_SERVICES("otherGoodsServices", "기타 상품 및 서비스", 720);

    private final String key;
    private final String label;
    private final long minIntervalMinutes;

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
