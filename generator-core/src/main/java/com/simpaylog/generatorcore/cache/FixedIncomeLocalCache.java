package com.simpaylog.generatorcore.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorcore.cache.dto.FixedIncomePolicy;
import com.simpaylog.generatorcore.enums.PreferenceType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class FixedIncomeLocalCache {
    private FixedIncomePolicy fixedIncomePolicy;
    private final Map<String, FixedIncomePolicy.SourceTemplate> byType = new ConcurrentHashMap<>();

    public Optional<FixedIncomePolicy.SourceTemplate> getSourceByType(String type) {
        return Optional.ofNullable(byType.get(type));
    }

    public double getProbabilityOfAssignmentRule(PreferenceType preferenceType, int decile) {
        if (fixedIncomePolicy == null) return 0.0;

        return fixedIncomePolicy.assignment()
                .getOrDefault(preferenceType, Map.of())
                .getOrDefault(decile, new FixedIncomePolicy.AssignmentRule(0.0))
                .probability();
    }

    public List<String> getMappingTypeByPreferenceTypeAndDecile(PreferenceType preferenceType, int decile) {
        var mapping = fixedIncomePolicy.mapping();
        if (mapping == null) return List.of();

        Map<Integer, List<String>> byDecile = mapping.getOrDefault(preferenceType, Map.of());
        return byDecile.getOrDefault(decile, List.of());
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = new ClassPathResource("fixed_income.json").getInputStream();
            FixedIncomePolicy loaded = mapper.readValue(input, FixedIncomePolicy.class);
            if(loaded.sources() != null) {
                for(FixedIncomePolicy.SourceTemplate s : loaded.sources()) {
                    byType.put(s.type(), s);
                }
            }
            fixedIncomePolicy = loaded;
            log.info("[FixedIncomeLocalCache] 캐시 로딩 완료");
        } catch (Exception e) {
            log.error("캐시 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
}
