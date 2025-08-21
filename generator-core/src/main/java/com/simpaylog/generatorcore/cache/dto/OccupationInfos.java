package com.simpaylog.generatorcore.cache.dto;

import java.util.List;

public record OccupationInfos(
        String lastUpdated,
        double[] ratios,
        List<Occupation> occupations
) {
   public record Occupation(
           int code,
           String occupationCategory,
           int averageMonthlyWage,
           double[] decileDistribution,
           List<AgeGroupInfo> ageGroupInfo
   ) {
   }

   public record AgeGroupInfo(
           int[] range,
           String label,
           double ratio,
           int[] dominantDeciles
   ) {
   }
}
