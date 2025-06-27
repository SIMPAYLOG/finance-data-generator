package com.simpaylog.generatorapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.OccupationInfos;
import com.simpaylog.generatorapi.dto.OccupationInfos.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class OccupationalLocalCache {
    private String lastUpdated;
    private double[] ratios;
    private final Map<Integer, Occupation> cache = new ConcurrentHashMap<>();

    public Occupation get(int code) {
        return cache.get(code);
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("occupation.json").getInputStream();
            OccupationInfos data = mapper.readValue(input, OccupationInfos.class);
            lastUpdated = data.lastUpdated();
            ratios = data.ratios();
            Map<Integer, Occupation> map = new HashMap<>();

            for(Occupation occ : data.occupations()) {
                map.put(occ.code(), occ);
            }
            cache.putAll(map);
            System.out.println("[OccupationCacheInitializer] 캐시 로딩 완료: " + map.size() + "개");
        } catch (Exception e) {
            System.err.println("캐시 초기화 중 오류 발생: " + e.getMessage());
        }
    }
}
