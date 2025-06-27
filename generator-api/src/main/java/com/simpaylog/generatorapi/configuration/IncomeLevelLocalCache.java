package com.simpaylog.generatorapi.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.IncomeLevelInfo;
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
public class IncomeLevelLocalCache {
    private final Map<Integer, IncomeLevelInfo> cache = new ConcurrentHashMap<>();

    public IncomeLevelInfo get(int decile) {
        return cache.get(decile);
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("income_level.json").getInputStream();
            List<IncomeLevelInfo> data = mapper.readValue(input, new TypeReference<>() {
            });
            Map<Integer, IncomeLevelInfo> map = new HashMap<>();

            for (IncomeLevelInfo info : data) {
                map.put(info.decile(), info);
            }
            cache.putAll(map);
            log.info(String.format("[IncomeLevelCacheInitializer] 캐시 로딩 완료: %d개", map.size()));
        } catch (Exception e) {
            log.error(String.format("캐시 초기화 중 오류 발생: %s", e.getMessage()));
        }
    }
}
