package com.simpaylog.generatorcore.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.simpaylog.generatorcore.session.SimulationSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;

    @Bean(name = "simulationSessionRedisTemplate")
    public RedisTemplate<String, SimulationSession> simulationSessionRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, SimulationSession> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<SimulationSession> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, SimulationSession.class);

        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.setDefaultSerializer(valueSerializer);

        return template;
    }

    @Bean
    public RedisTemplate<String, LocalDate> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, LocalDate> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<LocalDate> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, LocalDate.class);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplate.setDefaultSerializer(valueSerializer);

        return redisTemplate;
    }
}
