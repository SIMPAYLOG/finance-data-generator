package com.simpaylog.generatorcore.entity;

import com.simpaylog.generatorcore.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "profile_id")
    private UserBehaviorProfile userBehaviorProfile;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private BigDecimal balance;

    private BigDecimal debt;

    private String job;
}
