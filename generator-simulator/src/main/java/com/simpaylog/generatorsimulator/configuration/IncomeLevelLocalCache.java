package com.simpaylog.generatorsimulator.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.simpaylog.generatorsimulator.dto.IncomeLevelInfos;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class IncomeLevelLocalCache {
    private final Map<Integer, IncomeLevelInfos> incomeLevelCache = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            InputStream input = getClass().getClassLoader().getResourceAsStream("income_level.json");
            if (input == null) throw new IllegalStateException("income_level.json이 존재하지 않습니다.");

            incomeLevelCache.putAll(mapper.readValue(input, new TypeReference<>() {}));
        } catch (Exception e) {
            throw new RuntimeException("소득분위 정보 캐쉬 초기화 중 오류 발생 : ", e);
        }
    }

    public IncomeLevelInfos get(int id){
        return incomeLevelCache.get(id);
    }

    public Map<Integer, IncomeLevelInfos> getAll() {
        return incomeLevelCache;
    }
}
