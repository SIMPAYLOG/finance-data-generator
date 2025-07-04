package com.simpaylog.generatorsimulator.configuration;

import com.simpaylog.generatorcore.cache.PreferenceLocalCache;
import com.simpaylog.generatorsimulator.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@Import(PreferenceLocalCache.class)
public class PreferenceInfosLocalCacheTest extends TestConfig {
     private final String[] preferenceNames = new String[] {"", "소비지향형", "저축지향형", "무계획형", "투자지향형", "안정추구형"};

     @Autowired
     PreferenceLocalCache preferenceCache;

     @RepeatedTest(value = 5)
     @DisplayName("preference.json → 객체 매핑 검증")
     void loadPreferencesTest(RepetitionInfo repetitionInfo) {
         assertNotNull(preferenceCache.getAll());
         int preferenceId = repetitionInfo.getCurrentRepetition();
         assertEquals(preferenceNames[preferenceId], preferenceCache.get(preferenceId).name());
         assertNotNull(preferenceCache.get(preferenceId).totalConsumeRange());
         assertFalse(preferenceCache.get(preferenceId).tagConsumeRange().isEmpty());
         assertEquals(12, preferenceCache.get(preferenceId).tagConsumeRange().size());
     }
}
