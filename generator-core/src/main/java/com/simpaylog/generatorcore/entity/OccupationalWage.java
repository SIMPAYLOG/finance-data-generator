package com.simpaylog.generatorcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "occupational_wages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OccupationalWage{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String occupation;

    private Integer monthlyWage;
}
