package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.enums.WageType;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnTransformer;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "user_behavior_profiles")
public class UserBehaviorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Enumerated(EnumType.STRING)
    private PreferenceType preferenceType;
    @Enumerated(EnumType.STRING)
    private WageType wageType;
    private Integer autoTransferDayOfMonth;
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String activeHours;
    private BigDecimal incomeValue;
    private BigDecimal assetValue;
    private BigDecimal savingRate;

    protected UserBehaviorProfile() {
    }

    private UserBehaviorProfile(PreferenceType preferenceType, WageType wageType, int autoTransferDayOfMonth, BigDecimal incomeValue, BigDecimal assetValue, BigDecimal savingRate) {
        this.preferenceType = preferenceType;
        this.wageType = wageType;
        this.autoTransferDayOfMonth = autoTransferDayOfMonth;
        this.activeHours = "{\"min\": 7, \"max\": 23}";
        this.incomeValue = incomeValue;
        this.assetValue = assetValue;
        this.savingRate = savingRate;
    }

    public static UserBehaviorProfile of(PreferenceType preferenceType, WageType wageType, int autoTransferDayOfMonth,  BigDecimal incomeValue, BigDecimal assetValue, BigDecimal savingRate) {
        return new UserBehaviorProfile(preferenceType, wageType, autoTransferDayOfMonth, incomeValue, assetValue, savingRate);
    }
}
