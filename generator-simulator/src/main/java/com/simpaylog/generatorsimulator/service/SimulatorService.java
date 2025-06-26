package com.simpaylog.generatorsimulator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorsimulator.dto.internaldata.Preference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SimulatorService {
    private final Map<Integer, Preference> preferenceMap;

    public SimulatorService() {
        this.preferenceMap = loadPreferences();
    }

    // 1. JSON 불러오기
    private Map<Integer, Preference> loadPreferences() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // 클래스패스 기준 경로, resources/preference.json
            InputStream input = getClass().getClassLoader().getResourceAsStream("preference.json");
            if (input == null) throw new IllegalStateException("preference.json not found in classpath");

            return mapper.readValue(input, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load preference.json", e);
        }
    }
}
