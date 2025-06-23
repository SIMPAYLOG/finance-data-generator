package com.simpaylog.generatorcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Entity
@Table(name = "occupational_wages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OccupationalWage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occupation", length = 20)
    private String occupation;

    @Column(name = "monthly_wage")
    private Integer monthlyWage; // Integer로 매핑
}
