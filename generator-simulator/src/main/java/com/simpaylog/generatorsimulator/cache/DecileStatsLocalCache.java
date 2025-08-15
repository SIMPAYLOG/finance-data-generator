package com.simpaylog.generatorsimulator.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorsimulator.cache.dto.DecileStat;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class DecileStatsLocalCache {
    private final Map<Integer, DecileStat> cache = new ConcurrentHashMap<>();

    public DecileStat getDecileStat(int decile) {
        DecileStat stat = cache.get(decile);
        if (stat == null) throw new IllegalArgumentException("No stats for decile=" + decile);
        return stat;
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("stats.json").getInputStream();
            TypeReference<List<DecileStat>> typeRef = new TypeReference<>() {};
            List<DecileStat> decileStats = mapper.readValue(input, typeRef);

            for (DecileStat stat : decileStats) {
                cache.put(stat.decile(), stat);
            }
            log.info("[DecileStatsLocalCache] 캐시 로딩 완료: 10개 중 {}개", cache.size());
        } catch (Exception e) {
            log.error("캐시 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
}
