package com.simpaylog.generatorsimulator.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.simpaylog.generatorsimulator.dto.PreferenceInfos;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class PreferenceLocalCache {
    private final Map<Integer, PreferenceInfos> preferenceCache = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            InputStream input = getClass().getClassLoader().getResourceAsStream("preference.json");
            if (input == null) throw new IllegalStateException("preference.json not found in classpath");

            preferenceCache.putAll(mapper.readValue(input, new TypeReference<>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load preference.json", e);
        }
    }

    public PreferenceInfos get(int id){
        return preferenceCache.get(id);
    }

    public Map<Integer, PreferenceInfos> getAll() {
        return preferenceCache;
    }

}
