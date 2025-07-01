package com.simpaylog.generatorsimulator.dto;

import java.util.List;

public record PreferenceInfos(
        int id,
        String name,
        TotalConsumeRange totalConsumeRange,
        List<TagConsumeRange> tagConsumeRange)
{
    public record TotalConsumeRange(int min, int max) {}
    public record TagConsumeRange(String type, String typeKor, int min, int max) {}
}
