package com.simpaylog.generatorsimulator;

import com.simpaylog.generatorapi.configuration.IncomeLevelLocalCache;
import com.simpaylog.generatorcore.GeneratorCoreConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.name=application-core")
@SpringBootTest(classes = {GeneratorCoreConfiguration.class, IncomeLevelLocalCache.class})
public class TestConfig {
}