package com.simpaylog.generatorsimulator.dto;

import com.simpaylog.generatorcore.enums.TransactionFrequencyPattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class UserBehaviorProfileDto {
    private Long id;

    private Integer preferenceId;

    private BigDecimal spendingProbability;

    private TransactionFrequencyPattern transactionFrequencyPattern;

    private Integer incomeDayOfMonth;

    private BigDecimal autoTransferRatio;

    private BigDecimal averageSavingAmountRatio;

    private String averageSpendingAmountRange;

    private String activeHours;

    private String behaviorType;

    private BigDecimal incomeValue;

    private BigDecimal assetValue;

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

    private BigDecimal ConsumptionExpenditure;

    private BigDecimal nonConsumptionExpenditure;

    private BigDecimal surplusRatePct;

    private BigDecimal avgPropensityToConsumePct;
}
