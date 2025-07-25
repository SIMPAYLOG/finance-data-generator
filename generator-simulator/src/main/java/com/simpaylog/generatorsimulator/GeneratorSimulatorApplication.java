package com.simpaylog.generatorsimulator;

import com.simpaylog.generatorcore.GeneratorCoreConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(GeneratorCoreConfiguration.class)
@SpringBootApplication
public class GeneratorSimulatorApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "application-core,application-simulator");
		SpringApplication.run(GeneratorSimulatorApplication.class, args);
	}

}
