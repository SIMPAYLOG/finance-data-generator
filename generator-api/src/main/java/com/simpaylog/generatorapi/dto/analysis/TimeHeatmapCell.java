package com.simpaylog.generatorapi.dto.analysis;

import java.util.List;

public record TimeHeatmapCell(
        List<TCSummary> results
) {

    public record TCSummary(
            int dayOfWeek,
            int hour,
            int count
    ) {

    }
}
