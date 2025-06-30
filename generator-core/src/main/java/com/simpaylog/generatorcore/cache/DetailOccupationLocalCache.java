package com.simpaylog.generatorcore.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorcore.cache.dto.DetailOccupationInfo;
import com.simpaylog.generatorcore.cache.dto.DetailOccupationInfo.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class DetailOccupationLocalCache {
    private final Map<String, DetailOccupationInfo> cache = new ConcurrentHashMap<>();

    public Map<Integer, SubOccupation> getSubOccupations(String code) {
        return cache.get(code).subOccupations();
    }

    public SubOccupation getSubOccupationsByCodeAndDecile(String code, int decile) {
        return cache.get(code).subOccupations().get(decile);
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("detail_occupation.json").getInputStream();
            TypeReference<Map<String, DetailOccupationInfo>> typeRef = new TypeReference<>() {};

            Map<String, DetailOccupationInfo> data = mapper.readValue(input, typeRef);
            for (String key : data.keySet()) {
                cache.put(key, data.get(key));
            }
            log.info(String.format("[DetailOccupationLocalCache] 캐시 로딩 완료: %d개", cache.size()));
        } catch (Exception e) {
            log.error(String.format("캐시 초기화 중 오류 발생: %s", e.getMessage()));
        }
    }
}
