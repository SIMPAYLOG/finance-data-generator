package com.simpaylog.generatorcore.utils;

import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.enums.PreferenceType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class AccountFactory {

    public List<Account> generateAccountsFor(BigDecimal incomeValue, BigDecimal assetVale, int age, int decile, PreferenceType preferenceType) {
        List<Account> accounts = new ArrayList<>();
        accounts.add(createCheckingAccount(incomeValue, age, decile, preferenceType)); // 입출금 통장
        accounts.add(createSavingsAccount(assetVale)); // 예금 통장
        return accounts;
    }

    // 입출금
    private Account createCheckingAccount(BigDecimal incomeValue, int age, int decile, PreferenceType preferenceType) {
        BigDecimal balance = incomeValue.multiply(BigDecimal.valueOf(rand(0.5, 2.5)));
        return Account.ofChecking(balance, calculateOverdraftLimit(age, decile, preferenceType));
    }

    // 예금
    private Account createSavingsAccount(BigDecimal assetValue) {
        BigDecimal rate = BigDecimal.valueOf(rand(2.0, 3.5)).setScale(2, RoundingMode.HALF_UP);
        return Account.ofSavings(assetValue, rate);
    }

    private double rand(double min, double max) {
        return min + (Math.random() * (max - min));
    }

    private BigDecimal calculateOverdraftLimit(int age, int decile, PreferenceType preferenceType) {
        BigDecimal base = BigDecimal.valueOf(100_000); // 기준 한도

        // 연령대 계수
        double ageFactor = switch (age / 10 * 10) {
            case 10 -> 0.5;
            case 20 -> 0.8;
            case 30 -> 1.0;
            case 40, 50 -> 1.2;
            case 60 -> 1.0;
            default -> 0.8;
        };

        // 소득 분위 계수
        double decileFactor = switch (decile) {
            case 1, 2 -> 0.8;
            case 3, 4, 5 -> 1.0;
            case 6, 7, 8 -> 1.2;
            case 9, 10 -> 1.4;
            default -> 1.0;
        };

        // 소비 성향 계수
        double preferenceFactor = switch (preferenceType) {
            case CONSUMPTION_ORIENTED -> 1.5;
            case UNPLANNED -> 1.3;
            case STABLE -> 1.0;
            case SAVING_ORIENTED -> 0.7;
            case INVESTMENT_ORIENTED -> 1.1;
            default -> 1.0;
        };

        return base
                .multiply(BigDecimal.valueOf(ageFactor))
                .multiply(BigDecimal.valueOf(decileFactor))
                .multiply(BigDecimal.valueOf(preferenceFactor))
                .setScale(0, RoundingMode.HALF_UP);
    }

}
