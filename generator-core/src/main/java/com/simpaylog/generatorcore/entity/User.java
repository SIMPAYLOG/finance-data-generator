package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private UserBehaviorProfile userBehaviorProfile;
    private String name;
    private Integer decile;
    private Integer age;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private BigDecimal balance;
    private int jobNumber;
    private int occupationCode;
    private String occupationName;
    private Integer conditionId;

    protected User() {
    }

    private User(UserBehaviorProfile profile, int decile, int age, Gender gender, BigDecimal balance, int jobNumber, int occupationCode, String occupationName) {
        this.userBehaviorProfile = profile;
        this.decile = decile;
        this.age = age;
        this.gender = gender;
        this.balance = balance;
        this.jobNumber = jobNumber;
        this.occupationCode = occupationCode;
        this.occupationName = occupationName;
    }

    public static User of(UserBehaviorProfile profile, int decile, int age, Gender gender, BigDecimal balance, int jobNumber, int occupationCode, String occupationName) {
        return new User(profile, decile, age, gender, balance, jobNumber, occupationCode, occupationName);
    }
}
