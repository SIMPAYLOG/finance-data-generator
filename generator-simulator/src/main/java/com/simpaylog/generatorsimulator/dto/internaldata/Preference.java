package com.simpaylog.generatorsimulator.dto.internaldata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Preference {
    private int id;
    private String name;
    private TotalConsumeRange totalConsumeRange;
    private List<TagConsumeRange> tagConsumeRange;
}
