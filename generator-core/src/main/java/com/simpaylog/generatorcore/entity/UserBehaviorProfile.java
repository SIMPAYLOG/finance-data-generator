package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.TransactionFrequencyPattern;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "user_behavior_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorProfile{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal spendingProbability;

    @Enumerated(EnumType.STRING)
    private TransactionFrequencyPattern transactionFrequencyPattern;

    private Integer incomeDayOfMonth;

    private BigDecimal autoTransferRatio;

    private BigDecimal averageSavingAmountRatio;

    @Column(columnDefinition = "jsonb")
    private String averageSpendingAmountRange;

    @Column(columnDefinition = "jsonb")
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
