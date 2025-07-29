package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Setter
    private String sessionId;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    private UserBehaviorProfile userBehaviorProfile;
    private String name;
    private Integer decile;
    private Integer age;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Setter
    private BigDecimal balance;
    private int jobNumber;
    private int occupationCode;
    private String occupationName;
    private Integer conditionId;

    protected User() {
    }

    private User(String sessionId, String name, UserBehaviorProfile profile, int decile, int age, Gender gender, BigDecimal balance, int jobNumber, int occupationCode, String occupationName, int conditionId) {
        this.sessionId = sessionId;
        this.name = name;
        this.userBehaviorProfile = profile;
        this.decile = decile;
        this.age = age;
        this.gender = gender;
        this.balance = balance;
        this.jobNumber = jobNumber;
        this.occupationCode = occupationCode;
        this.occupationName = occupationName;
        this.conditionId = conditionId;
    }

    public static User of(String name, UserBehaviorProfile profile, int decile, int age, Gender gender, BigDecimal balance, int jobNumber, int occupationCode, String occupationName, int conditionId) {
        return new User(null, name, profile, decile, age, gender, balance, jobNumber, occupationCode, occupationName, conditionId);
    }

    public static User of(String sessionId, String name, UserBehaviorProfile profile, int decile, int age, Gender gender, BigDecimal balance, int jobNumber, int occupationCode, String occupationName, int conditionId) {
        return new User(sessionId, name, profile, decile, age, gender, balance, jobNumber, occupationCode, occupationName, conditionId);
    }
}
