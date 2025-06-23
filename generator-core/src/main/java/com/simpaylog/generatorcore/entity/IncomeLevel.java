package com.simpaylog.generatorcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "income_level")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncomeLevel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "income_range", columnDefinition = "jsonb")
    private String incomeRange;

    @Column(name = "asset_range", columnDefinition = "jsonb")
    private String assetRange;

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

