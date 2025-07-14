package com.simpaylog.generatorcore.dto.response;

import com.simpaylog.generatorcore.dto.analyze.AgeStat;
import com.simpaylog.generatorcore.dto.analyze.GenderStat;
import com.simpaylog.generatorcore.dto.analyze.OccupationNameStat;

import java.util.List;

public record UserAnalyzeResultResponse(
        Long totalUsers,
        List<AgeStat> ageDistribution,
        List<OccupationNameStat> occupationDistribution,
        GenderStat genderDistribution
) {

}
