package com.simpaylog.generatorcore.entity;

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
    private Integer preferenceId;
    @Enumerated(EnumType.STRING)
    private WageType wageType;
    private Integer autoTransferDayOfMonth;
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String activeHours;
    private BigDecimal incomeValue;
    private BigDecimal assetValue;

    protected UserBehaviorProfile() {
    }

    protected UserBehaviorProfile( BigDecimal incomeValue, int preferenceId, WageType wageType, int autoTransferDayOfMonth) {
        this.preferenceId = preferenceId;
        this.wageType = wageType;
        this.incomeValue = incomeValue;
        this.autoTransferDayOfMonth = autoTransferDayOfMonth;
        this.activeHours = "{\"min\": 7, \"max\": 23}";
        this.assetValue = BigDecimal.ZERO;
    }

    public static UserBehaviorProfile of(BigDecimal incomeValue, int preferenceId, WageType wageType, int autoTransferDayOfMonth) {
        return new UserBehaviorProfile(incomeValue, preferenceId, wageType, autoTransferDayOfMonth);
    }
}
