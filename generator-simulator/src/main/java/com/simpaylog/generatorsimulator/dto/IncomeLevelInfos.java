package com.simpaylog.generatorsimulator.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IncomeLevelInfos {
    private BigDecimal groceriesNonAlcoholicBeverages;
    private BigDecimal alcoholicBeveragesTobacco;
    private BigDecimal clothingFootwear;
    private BigDecimal housingUtilitiesFuel;
    private BigDecimal householdGoodsServices;
    private BigDecimal health;
    private BigDecimal transportation;
    private BigDecimal communication;
    private BigDecimal recreationCulture;
    private BigDecimal education;
    private BigDecimal foodAccommodation;
    private BigDecimal otherGoodsServices;

    public BigDecimal getCost(String tag){
        BigDecimal cost = BigDecimal.ZERO;
        switch (tag){
            case "groceriesNonAlcoholicBeverages":
                cost = getGroceriesNonAlcoholicBeverages();
                break;
            case "alcoholicBeveragesTobacco":
                cost = getAlcoholicBeveragesTobacco();
                break;
            case "clothingFootwear":
                cost = getClothingFootwear();
                break;
            case "housingUtilitiesFuel":
                cost = getHousingUtilitiesFuel();
                break;
            case "householdGoodsServices":
                cost = getHouseholdGoodsServices();
                break;
            case "health":
                cost = getHealth();
                break;
            case "transportation":
                cost = getTransportation();
                break;
            case "communication":
                cost = getCommunication();
                break;
            case "recreationCulture":
                cost = getRecreationCulture();
                break;
            case "education":
                cost = getEducation();
                break;
            case "foodAccommodation":
                cost = getFoodAccommodation();
                break;
            case "otherGoodsServices":
                cost = getOtherGoodsServices();
                break;
            default:
                System.out.println("tag 이름이 일치하는 get 함수 없음 (" + tag + ")");
        }

        return cost;
    }
}
