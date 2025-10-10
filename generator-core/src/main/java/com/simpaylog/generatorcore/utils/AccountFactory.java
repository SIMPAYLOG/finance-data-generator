package com.simpaylog.generatorcore.utils;

import com.simpaylog.generatorcore.entity.Account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class AccountFactory {

    public List<Account> generateAccountsFor(BigDecimal incomeValue, BigDecimal assetVal) {
        List<Account> accounts = new ArrayList<>();
        accounts.add(createCheckingAccount(incomeValue)); // 입출금 통장
        accounts.add(createSavingsAccount(assetVal)); // 저금 통장
        return accounts;
    }

    // 입출금
    private Account createCheckingAccount(BigDecimal incomeValue) {
        BigDecimal balance = incomeValue.multiply(BigDecimal.valueOf(rand(0.5, 2.5)));
        return Account.ofChecking(balance);
    }

    // 저금
    private Account createSavingsAccount(BigDecimal assetValue) {
        BigDecimal rate = BigDecimal.valueOf(rand(2.0, 3.5)).setScale(2, RoundingMode.HALF_UP);
        return Account.ofSavings(assetValue, rate);
    }

    private double rand(double min, double max) {
        return min + (Math.random() * (max - min));
    }

}
