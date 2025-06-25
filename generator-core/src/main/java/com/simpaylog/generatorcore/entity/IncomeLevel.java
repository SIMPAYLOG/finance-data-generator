package com.simpaylog.generatorcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "income_level")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncomeLevel{
    @Id
    private Long id;

    @Column(columnDefinition = "jsonb")
    private String assetRange;

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

