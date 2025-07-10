package com.simpaylog.generatorcore.dto.response;

import com.simpaylog.generatorcore.dto.*;

import java.util.List;

public record UserAnalyzeResultResponse(
        Long totalUsers,
        List<AgeStats> ageDistribution,
        List<OccupationNameStats> occupationDistribution,
        GenderStats genderDistribution
) {

}
