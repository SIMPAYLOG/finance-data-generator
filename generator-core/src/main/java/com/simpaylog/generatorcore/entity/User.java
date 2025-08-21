package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    private int occupationCode;
    private String occupationName;
    private Integer conditionId;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Account> accounts;

    protected User() {
    }

    private User(String name, UserBehaviorProfile profile, int decile, int age, Gender gender, int occupationCode, String occupationName, int conditionId, List<Account> accounts) {
        this.name = name;
        this.userBehaviorProfile = profile;
        this.decile = decile;
        this.age = age;
        this.gender = gender;
        this.occupationCode = occupationCode;
        this.occupationName = occupationName;
        this.conditionId = conditionId;
        this.accounts = accounts;
    }

    public static User of(String name, UserBehaviorProfile profile, int decile, int age, Gender gender, int occupationCode, String occupationName, int conditionId, List<Account> accounts) {
        User user = new User(name, profile, decile, age, gender, occupationCode, occupationName, conditionId, accounts);
        for (Account account : accounts) {
            account.setUser(user);
        }

        return user;
    }

}
