package com.simpaylog.generatorcore.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.simpaylog.generatorcore.cache.dto.Location;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class LocationLocalCache {
    private final Map<Integer, Location> cache = new ConcurrentHashMap<>();

    public Location getLocation(int id) {
        Location location = cache.get(id);
        if (location == null) throw new IllegalArgumentException("No stats for location id=" + id);
        return location;
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("locations.json").getInputStream();
            TypeReference<List<Location>> typeRef = new TypeReference<>() {};
            List<Location> locations = mapper.readValue(input, typeRef);

            for (Location loc : locations) {
                cache.put(loc.id(), loc);
            }
            log.info("[LocationLocalCache] 캐시 로딩 완료: {}개 중 {}개", locations.size(), cache.size());
        } catch (Exception e) {
            log.error("캐시 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
}
