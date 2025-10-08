package com.simpaylog.generatorcore.cache.dto;

import java.math.BigDecimal;
import java.util.List;

public record Location(
        int id,
        String location,
        List<NeighborLocation> neighbors
) {
    public record NeighborLocation(
            String location,    //위치
            BigDecimal weight //가중치
    ) {}
}
