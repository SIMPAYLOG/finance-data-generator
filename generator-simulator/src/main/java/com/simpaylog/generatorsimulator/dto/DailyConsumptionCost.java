package com.simpaylog.generatorsimulator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DailyConsumptionCost {
    private String date;
    private Long groceriesNonAlcoholicBeverages;
    private Long alcoholicBeveragesTobacco;
    private Long clothingFootwear;
    private Long housingUtilitiesFuel;
    private Long householdGoodsServices;
    private Long health;
    private Long transportation;
    private Long communication;
    private Long recreationCulture;
    private Long education;
    private Long foodAccommodation;
    private Long otherGoodsServices;

    public boolean setDailyCost(String tag, long cost){
        switch (tag){
            case "groceriesNonAlcoholicBeverages":
                setGroceriesNonAlcoholicBeverages(cost);
                break;
            case "alcoholicBeveragesTobacco":
                setAlcoholicBeveragesTobacco(cost);
                break;
            case "clothingFootwear":
                setClothingFootwear(cost);
                break;
            case "housingUtilitiesFuel":
                setHousingUtilitiesFuel(cost);
                break;
            case "householdGoodsServices":
                setHouseholdGoodsServices(cost);
                break;
            case "health":
                setHealth(cost);
                break;
            case "transportation":
                setTransportation(cost);
                break;
            case "communication":
                setCommunication(cost);
                break;
            case "recreationCulture":
                setRecreationCulture(cost);
                break;
            case "education":
                setEducation(cost);
                break;
            case "foodAccommodation":
                setFoodAccommodation(cost);
                break;
            case "otherGoodsServices":
                setOtherGoodsServices(cost);
                break;
            default:
                System.out.println("tag 이름이 일치하는 set 함수 없음 (" + tag + ")");
                return false;
        }

        return true;
    }
}
