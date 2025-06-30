package com.simpaylog.generatorapi.dto;

import com.simpaylog.generatorcore.enums.WageType;

import java.util.List;
import java.util.Map;

public record DetailOccupationInfo(
        int occupationCode,
        Map<Integer, SubOccupation> subOccupations
) {
    public record SubOccupation(
            List<Job> jobs
    ) {}

    public record Job(
            String jobTitle,
            WageType wageType
    ) {}
}

