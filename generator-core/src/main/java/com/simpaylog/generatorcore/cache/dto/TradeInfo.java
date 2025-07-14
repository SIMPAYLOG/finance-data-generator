package com.simpaylog.generatorcore.cache.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TradeInfo(
        @JsonProperty("decile") int decile,
        @JsonProperty("category") List<CategoryDetail> category
) {
    // category 배열 내의 각 카테고리 정보를 위한 중첩 레코드
    public record CategoryDetail(
            @JsonProperty("name") String name,
            @JsonProperty("weights") List<Double> weights,
            @JsonProperty("trades") List<TradeItemDetail> trades
    ) {}

    // trades 배열 내의 각 거래 항목을 위한 중첩 레코드
    public record TradeItemDetail(
            @JsonProperty("name") String name,
            @JsonProperty("min") int min,
            @JsonProperty("max") int max
    ) {}
}