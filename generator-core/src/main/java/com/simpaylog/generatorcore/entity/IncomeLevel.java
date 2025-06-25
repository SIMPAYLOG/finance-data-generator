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

    @Column(columnDefinition = "jsonb")
    private String incomeRange;

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

    private BigDecimal nonConsumptionExpenditure;

    private BigDecimal surplusRatePct;

    private BigDecimal avgPropensityToConsumePct;
}

