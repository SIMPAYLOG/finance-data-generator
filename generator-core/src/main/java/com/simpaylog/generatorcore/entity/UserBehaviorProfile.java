package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.TransactionFrequencyPattern;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.math.BigDecimal;

@Entity
@Table(name = "user_behavior_profiles")
@Getter
@Setter
public class UserBehaviorProfile{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer preferenceId;

    private BigDecimal spendingProbability;

    @Enumerated(EnumType.STRING)
    private TransactionFrequencyPattern transactionFrequencyPattern;

    private Integer incomeDayOfMonth;

    private BigDecimal autoTransferRatio;

    private BigDecimal averageSavingAmountRatio;

    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String averageSpendingAmountRange;

    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
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

    protected UserBehaviorProfile(){}

    protected UserBehaviorProfile(BigDecimal incomeValue, int incomeDayOfMonth) {
        this.incomeValue = incomeValue;
        this.incomeDayOfMonth = incomeDayOfMonth;
        this.transactionFrequencyPattern = TransactionFrequencyPattern.REGULAR;

        this.preferenceId = 1;
        this.spendingProbability = BigDecimal.ZERO;
        this.autoTransferRatio = BigDecimal.ZERO;
        this.averageSavingAmountRatio = BigDecimal.ZERO;
        this.averageSpendingAmountRange = "\"hello\"";
        this.activeHours = "\"hello\"";
        this.behaviorType = "";
        this.assetValue = BigDecimal.ZERO;
        this.groceriesNonAlcoholicBeverages = BigDecimal.ZERO;
        this.alcoholicBeveragesTobacco = BigDecimal.ZERO;
        this.clothingFootwear = BigDecimal.ZERO;
        this.housingUtilitiesFuel = BigDecimal.ZERO;
        this.householdGoodsServices = BigDecimal.ZERO;
        this.health = BigDecimal.ZERO;
        this.transportation = BigDecimal.ZERO;
        this.communication = BigDecimal.ZERO;
        this.recreationCulture = BigDecimal.ZERO;
        this.education = BigDecimal.ZERO;
        this.foodAccommodation = BigDecimal.ZERO;
        this.otherGoodsServices = BigDecimal.ZERO;
        ConsumptionExpenditure = BigDecimal.ZERO;
        this.nonConsumptionExpenditure = BigDecimal.ZERO;
        this.surplusRatePct = BigDecimal.ZERO;
        this.avgPropensityToConsumePct = BigDecimal.ZERO;
    }

    public static UserBehaviorProfile of(BigDecimal incomeValue, int incomeDayOfMonth) {
        return new UserBehaviorProfile(incomeValue, incomeDayOfMonth);
    }
}
