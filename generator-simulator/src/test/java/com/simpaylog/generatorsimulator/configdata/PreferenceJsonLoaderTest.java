package com.simpaylog.generatorsimulator.configdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.simpaylog.generatorsimulator.dto.internaldata.Preference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.InputStream;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PreferenceJsonLoaderTest {
     String[] preferenceNames = new String[] {"", "소비지향형", "저축지향형", "무계획형", "투자지향형", "안정추구형"};

     @Test
     @DisplayName("preference.json → 객체 매핑 검증")
     void loadPreferencesTest() {
         Map<Integer, Preference> preferenceMap = loadPreferences();
         assertNotNull(preferenceMap);
         for(int i = 1; i <= 5; i++){
             assertEquals(preferenceNames[i], preferenceMap.get(i).getName());
             assertNotNull(preferenceMap.get(i).getTotalConsumeRange());
             assertFalse(preferenceMap.get(i).getTagConsumeRange().isEmpty());
             assertEquals(12, preferenceMap.get(i).getTagConsumeRange().size());
         }
     }

    private Map<Integer, Preference> loadPreferences() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            InputStream input = getClass().getClassLoader().getResourceAsStream("preference.json");
            if (input == null) throw new IllegalStateException("preference.json not found in classpath");

            return mapper.readValue(input, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load preference.json", e);
        }
    }
}
