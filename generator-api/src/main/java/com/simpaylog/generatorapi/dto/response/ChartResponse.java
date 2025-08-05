package com.simpaylog.generatorapi.dto.response;

import com.simpaylog.generatorapi.dto.chart.ChartCategoryDto;

import java.util.List;

public record ChartResponse(
    String chartType,
    String title,
    String xAxisLabel,
    String yAxisLabel,
    List<ChartCategoryDto> data
) {}