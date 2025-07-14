package com.simpaylog.generatorcore.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorcore.cache.dto.TradeInfo; // 새로 만든 DTO 임포트
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class TradeInfoLocalCache {

    private final Map<Integer, Map<String, TradeInfo.CategoryDetail>> cache = new ConcurrentHashMap<>();

    public List<Double> getWeights(int decile, String categoryName) {
        Map<String, TradeInfo.CategoryDetail> categoryMap = cache.get(decile);
        if (categoryMap != null) {
            TradeInfo.CategoryDetail categoryDetail = categoryMap.get(categoryName);
            if (categoryDetail != null) {
                return categoryDetail.weights();
            }
        }
        return new ArrayList<>(); //입력값이 에러인 경우 생각하기
    }

    public List<TradeInfo.TradeItemDetail> getTradeList(int decile, String categoryName) {
        Map<String, TradeInfo.CategoryDetail> categoryMap = cache.get(decile);
        if (categoryMap != null) {
            TradeInfo.CategoryDetail categoryDetail = categoryMap.get(categoryName);
            if (categoryDetail != null) {
                return categoryDetail.trades();
            }
        }
        return new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("trade_info.json").getInputStream();
            List<TradeInfo> dataList = mapper.readValue(input, new TypeReference<>() {
            });
            Map<Integer, Map<String, TradeInfo.CategoryDetail>> tempCache = new HashMap<>();

            for (TradeInfo decileInfo : dataList) {
                Map<String, TradeInfo.CategoryDetail> categoryMap = new HashMap<>();
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