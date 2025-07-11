package com.simpaylog.generatorsimulator.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorsimulator.dto.CategorySpendingWeight;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class CategoryPreferenceWeightLocalCache {
    private final Map<String, CategorySpendingWeight> cache = new ConcurrentHashMap<>();

    public CategorySpendingWeight getCategorySpendingWeight(String preferenceTypeKey) {
        if(!cache.containsKey(preferenceTypeKey)) return null;
        return cache.get(preferenceTypeKey);
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("category_preference_weights.json").getInputStream();
            TypeReference<Map<String, CategorySpendingWeight>> typeRef = new TypeReference<>() {};
            Map<String, CategorySpendingWeight> data = mapper.readValue(input, typeRef);
            for(String key : data.keySet()) {
                cache.put(key, data.get(key));
            }
            log.info("[CategorySpendingWeightLocalCache] 캐시 로딩 완료: {}개", cache.size());
        } catch (Exception e) {
            log.error("캐시 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
}
