package com.simpaylog.generatorsimulator.dto.internaldata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TagConsumeRange {
    private String type;
    private int min;
    private int max;
}
