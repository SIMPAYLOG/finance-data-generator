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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", unique = true, nullable = false)
    private UserBehaviorProfile userBehaviorProfile;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "balance")
    private Long balance;

    @Column(name = "debt")
    private Long debt;

    @Column(name = "job", length = 100)
    private String job;
}
