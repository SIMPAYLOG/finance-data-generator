package com.simpaylog.generatorcore.entity;

import java.io.Serializable;

import com.simpaylog.generatorcore.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // 모든 필드를 포함하는 생성자가 필요할 경우
import lombok.ToString;
import lombok.EqualsAndHashCode; // equals()와 hashCode() 자동 생성


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "profile_id")
    private UserBehaviorProfile userBehaviorProfile;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Long balance;

    private Long debt;

    private String job;
}
