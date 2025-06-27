package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private UserBehaviorProfile userBehaviorProfile;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private BigDecimal balance;

    private BigDecimal debt;

    private int jobNumber;

    private int occupationCode;

    protected User() {
    }

    private User(UserBehaviorProfile profile, int age, Gender gender, BigDecimal balance, BigDecimal debt, int jobNumber, int occupationCode) {
        this.userBehaviorProfile = profile;
        this.age = age;
        this.gender = gender;
        this.balance = balance;
        this.debt = debt;
        this.jobNumber = jobNumber;
        this.occupationCode = occupationCode;
    }

    public static User of(UserBehaviorProfile profile, int age, Gender gender, BigDecimal balance, BigDecimal debt, int jobNumber, int occupationCode) {
        return new User(profile, age, gender, balance, debt, jobNumber, occupationCode);
    }
}
