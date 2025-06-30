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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer preferenceId;
    @Enumerated(EnumType.STRING)
    private WageType transactionFrequencyPattern;
    private Integer incomeDayOfMonth;
    private Integer autoTransferDayOfMonth;
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String activeHours;
    private BigDecimal incomeValue;
    private BigDecimal assetValue;

    protected UserBehaviorProfile() {
    }

    protected UserBehaviorProfile( BigDecimal incomeValue, int preferenceId, int incomeDayOfMonth, int autoTransferDayOfMonth) {
        this.preferenceId = preferenceId;
        this.transactionFrequencyPattern = WageType.REGULAR;
        this.incomeDayOfMonth = incomeDayOfMonth;
        this.incomeValue = incomeValue;
        this.autoTransferDayOfMonth = autoTransferDayOfMonth;
        this.activeHours = "\"hello\"";
        this.assetValue = BigDecimal.ZERO;
    }

    public static UserBehaviorProfile of(BigDecimal incomeValue, int preferenceId, int incomeDayOfMonth, int autoTransferDayOfMonth) {
        return new UserBehaviorProfile(incomeValue, preferenceId, incomeDayOfMonth, autoTransferDayOfMonth);
    }
}
