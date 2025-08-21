package com.simpaylog.generatorsimulator.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorsimulator.cache.dto.CategorySpendingPattern;
import com.simpaylog.generatorcore.dto.CategoryType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@Getter
@Component
public class CategorySpendingPatternLocalCache {
    private CategorySpendingPattern categorySpendingPattern;

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("category_spending_patterns.json").getInputStream();
            Map<String, CategorySpendingPattern.Config> raw = mapper.readValue(input, new TypeReference<>() {});
            Map<CategoryType, CategorySpendingPattern.Config> converted =
                    raw.entrySet().stream()
                            .collect(java.util.stream.Collectors.toMap(
                                    e -> CategoryType.fromKey(e.getKey()),
                                    Map.Entry::getValue
                            ));
            this.categorySpendingPattern = new CategorySpendingPattern(converted);
            log.info("[CategorySpendingPatternLocalCache] 패턴 로딩 완료: {}개", converted.size());
        } catch (Exception e) {
            log.error("캐시 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
}