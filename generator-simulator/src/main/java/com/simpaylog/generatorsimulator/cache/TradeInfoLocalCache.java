package com.simpaylog.generatorsimulator.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorsimulator.cache.dto.TradeInfo;
import com.simpaylog.generatorcore.dto.CategoryType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class TradeInfoLocalCache {

    private final Map<Integer, Map<CategoryType, TradeInfo.CategoryDetail>> cache = new ConcurrentHashMap<>();

    public List<Double> getWeights(int decile, CategoryType categoryType) {
        Map<CategoryType, TradeInfo.CategoryDetail> categoryMap = cache.get(decile);
        TradeInfo.CategoryDetail categoryDetail = categoryMap.get(categoryType);
        return categoryDetail.weights();
    }

    public List<TradeInfo.TradeItemDetail> getTradeList(int decile, CategoryType categoryType) {
        Map<CategoryType, TradeInfo.CategoryDetail> categoryMap = cache.get(decile);
        TradeInfo.CategoryDetail categoryDetail = categoryMap.get(categoryType);
        return categoryDetail.trades();
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("trade_info.json").getInputStream();
            List<TradeInfo> dataList = mapper.readValue(input, new TypeReference<>() {
            });
            Map<Integer, Map<CategoryType, TradeInfo.CategoryDetail>> tempCache = new HashMap<>();

            for (TradeInfo decileInfo : dataList) {
                Map<CategoryType, TradeInfo.CategoryDetail> categoryMap = new HashMap<>();
                for (TradeInfo.CategoryDetail categoryDetail : decileInfo.category()) {
                    categoryMap.put(categoryDetail.name(), categoryDetail);
                }
                tempCache.put(decileInfo.decile(), categoryMap);
            }

            cache.putAll(tempCache);
            log.info(String.format("[TradeInfoLocalCache] 캐시 로딩 완료: %d개 분위 정보 로드", cache.size()));
        } catch (Exception e) {
            log.error(String.format("TradeInfoLocalCache 초기화 중 오류 발생: %s", e.getMessage()), e);
        }
    }
}