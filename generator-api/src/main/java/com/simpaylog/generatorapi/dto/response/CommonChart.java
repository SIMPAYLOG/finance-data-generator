package com.simpaylog.generatorapi.dto.response;

import java.util.List;

public record CommonChart<T>(
        String chartType,
        String title,
        String xAxisLabel,
        String yAxisLabel,
        List<T> data
) {
}
