package com.simpaylog.generatorsimulator.configuration;

import com.simpaylog.generatorsimulator.dto.PreferenceInfos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PreferenceInfosLocalCacheTest {
     private final String[] preferenceNames = new String[] {"", "소비지향형", "저축지향형", "무계획형", "투자지향형", "안정추구형"};

     @Autowired
     PreferenceLocalCache preferenceCache;

     @Test
     @DisplayName("preference.json → 객체 매핑 검증")
     void loadPreferencesTest() {
         Map<Integer, PreferenceInfos> preferences = preferenceCache.getAll();
         assertNotNull(preferences);
         for(int i = 1; i <= 5; i++){
             assertEquals(preferenceNames[i], preferences.get(i).name());
             assertNotNull(preferences.get(i).totalConsumeRange());
             assertFalse(preferences.get(i).tagConsumeRange().isEmpty());
             assertEquals(12, preferences.get(i).tagConsumeRange().size());
         }
     }
}
