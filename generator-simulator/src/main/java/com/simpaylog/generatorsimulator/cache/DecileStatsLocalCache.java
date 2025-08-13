package com.simpaylog.generatorsimulator.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorsimulator.cache.dto.DecileStat;
import com.simpaylog.generatorsimulator.dto.CategorySpendingWeight;
import com.simpaylog.generatorsimulator.dto.CategoryType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class DecileStatsLocalCache {
    private final Map<Integer, Map<CategoryType, BigDecimal>> cache = new ConcurrentHashMap<>();

    public Map<CategoryType, BigDecimal> getDecileStat(int decile) {
        Map<CategoryType, BigDecimal> env = cache.get(decile);
        if (env == null) throw new IllegalArgumentException("No stats for decile=" + decile);
        return new EnumMap<>(env); // 복사본
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("stats.json").getInputStream();
            TypeReference<List<DecileStat>> typeRef = new TypeReference<>() {};
            List<DecileStat> decileStats = mapper.readValue(input, typeRef);
            for(DecileStat stat : decileStats) {
                Map<CategoryType, BigDecimal> env = new EnumMap<>(CategoryType.class);
                stat.byCategory().forEach((k, v) -> env.put(CategoryType.fromKey(k), v));
                cache.put(stat.decile(), env);
            }
            log.info("[DecileStatsLocalCache] 캐시 로딩 완료: 10개 중 {}개", cache.size());
        } catch (Exception e) {
            log.error("캐시 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
}
