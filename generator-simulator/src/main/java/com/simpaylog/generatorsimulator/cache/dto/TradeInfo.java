package com.simpaylog.generatorsimulator.cache.dto;

import com.simpaylog.generatorsimulator.dto.CategoryType;

import java.util.List;

public record TradeInfo(
        int decile,
        List<CategoryDetail> category
) {
    // category 배열 내의 각 카테고리 정보를 위한 중첩 레코드
    public record CategoryDetail(
            CategoryType name,
            List<Double> weights,
            List<TradeItemDetail> trades
    ) {
    }

    // trades 배열 내의 각 거래 항목을 위한 중첩 레코드
    public record TradeItemDetail(
            String name,
            int min,
            int max
    ) {
    }
}