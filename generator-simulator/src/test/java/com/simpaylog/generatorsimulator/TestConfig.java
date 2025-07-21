package com.simpaylog.generatorsimulator;

import com.simpaylog.generatorcore.GeneratorCoreConfiguration;
import com.simpaylog.generatorsimulator.cache.CategoryPreferenceWeightLocalCache;
import com.simpaylog.generatorsimulator.cache.CategorySpendingPatternLocalCache;
import com.simpaylog.generatorsimulator.cache.TradeInfoLocalCache;
import com.simpaylog.generatorsimulator.service.TradeGenerator;
import com.simpaylog.generatorsimulator.service.TransactionGenerator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Import({CategoryPreferenceWeightLocalCache.class, CategorySpendingPatternLocalCache.class, TradeInfoLocalCache.class})
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.name=application-core")
@SpringBootTest(classes = GeneratorCoreConfiguration.class)
public class TestConfig {
}