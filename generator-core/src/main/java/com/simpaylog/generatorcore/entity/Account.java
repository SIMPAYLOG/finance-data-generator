package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "accounts")
public class Account {
    // TODO: 입출금 내역 뽑기
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Enumerated(EnumType.STRING)
    private AccountType type;
    @Setter
    private BigDecimal balance;
    private BigDecimal interestRate;
    @Column(precision = 12, scale = 2)
    private BigDecimal overdraftLimit;

    protected Account() {
    }

    private Account(AccountType type, BigDecimal balance, BigDecimal interestRate, BigDecimal overdraftLimit) {
        this.type = type;
        this.balance = balance;
        this.interestRate = interestRate;
        this.overdraftLimit = overdraftLimit;
    }

    public static Account ofChecking(BigDecimal balance, BigDecimal overdraftLimit) {
        return new Account(AccountType.CHECKING, balance, BigDecimal.ZERO, overdraftLimit);
    }

    public static Account ofSavings(BigDecimal balance, BigDecimal interestRate) {
        return new Account(AccountType.SAVINGS, balance, interestRate, BigDecimal.ZERO);
    }

}
