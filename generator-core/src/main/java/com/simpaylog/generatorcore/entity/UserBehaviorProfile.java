package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.TransactionFrequencyPattern;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "user_behavior_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorProfile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL의 BIGSERIAL에 매핑
    private Long id;

    @Column(name = "spending_probability", precision = 2, scale = 1)
    private BigDecimal spendingProbability;

    @Enumerated(EnumType.STRING) // Enum 값을 문자열로 저장
    @Column(name = "transaction_frequency_pattern")
    private TransactionFrequencyPattern transactionFrequencyPattern;

    @Column(name = "income_day_of_month")
    private Integer incomeDayOfMonth; // SMALLINT는 Integer로 매핑

    @Column(name = "auto_transfer_ratio", precision = 2, scale = 1)
    private BigDecimal autoTransferRatio;

    @Column(name = "average_saving_amount_ratio", precision = 2, scale = 1)
    private BigDecimal averageSavingAmountRatio;

    @Column(name = "average_spending_amount_range", columnDefinition = "jsonb")
    private String averageSpendingAmountRange;

    @Column(name = "active_hours", columnDefinition = "jsonb")
    private String activeHours;

    @Column(name = "behavior_type", length = 50)
    private String behaviorType;

    @Column(name = "income_value", precision = 15, scale = 2)
    private BigDecimal incomeValue;

    @Column(name = "asset_value", precision = 15, scale = 2)
    private BigDecimal assetValue;

    @Column(name = "groceries_non_alcoholic_beverages", precision = 5, scale = 2)
    private BigDecimal groceriesNonAlcoholicBeverages;

    @Column(name = "alcoholic_beverages_tobacco", precision = 5, scale = 2)
    private BigDecimal alcoholicBeveragesTobacco;

    @Column(name = "clothing_footwear", precision = 5, scale = 2)
    private BigDecimal clothingFootwear;

    @Column(name = "housing_utilities_fuel", precision = 5, scale = 2)
    private BigDecimal housingUtilitiesFuel;

    @Column(name = "household_goods_services", precision = 5, scale = 2)
    private BigDecimal householdGoodsServices;

    @Column(name = "health", precision = 5, scale = 2)
    private BigDecimal health;

    @Column(name = "transportation", precision = 5, scale = 2)
    private BigDecimal transportation;

    @Column(name = "communication", precision = 5, scale = 2)
    private BigDecimal communication;

    @Column(name = "recreation_culture", precision = 5, scale = 2)
    private BigDecimal recreationCulture;

    @Column(name = "education", precision = 5, scale = 2)
    private BigDecimal education;

    @Column(name = "food_accommodation", precision = 5, scale = 2)
    private BigDecimal foodAccommodation;

    @Column(name = "other_goods_services", precision = 5, scale = 2)
    private BigDecimal otherGoodsServices;

    @Column(name = "non_consumption_expenditure", precision = 5, scale = 2)
    private BigDecimal nonConsumptionExpenditure;

    @Column(name = "surplus_rate_pct", precision = 5, scale = 2)
    private BigDecimal surplusRatePct;

    @Column(name = "avg_propensity_to_consume_pct", precision = 5, scale = 2)
    private BigDecimal avgPropensityToConsumePct;
}
