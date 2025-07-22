package com.simpaylog.generatorapi;

import com.simpaylog.generatorcore.GeneratorCoreConfiguration;
import com.simpaylog.generatorsimulator.GeneratorSimulatorConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Import({GeneratorCoreConfiguration.class, GeneratorSimulatorConfiguration.class})
@SpringBootApplication
public class GeneratorApiApplication {
    public static void main(String[] args) {
        System.setProperty("spring.config.name", "application-core,application-api,application-simulator");
        SpringApplication.run(GeneratorApiApplication.class, args);
    }
}
