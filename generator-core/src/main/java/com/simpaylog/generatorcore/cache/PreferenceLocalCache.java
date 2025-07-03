package com.simpaylog.generatorcore.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorcore.cache.dto.preference.PreferenceInfos;
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
public class PreferenceLocalCache {
    private final Map<Integer, PreferenceInfos> cache = new ConcurrentHashMap<>();

    public PreferenceInfos get(int id){
        return cache.get(id);
    }

    public Map<Integer, PreferenceInfos> getAll() {
        return cache;
    }

    public int getKeySize() {
        return cache.size();
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("preference.json").getInputStream();
            cache.putAll(mapper.readValue(input, new TypeReference<>() {}));
            log.info(String.format("[PreferenceCacheInitializer] 캐시 로딩 완료: %d개", cache.size()));
        } catch (Exception e) {
            log.error(String.format("캐시 초기화 중 오류 발생: %s", e.getMessage()));
        }
    }
}
