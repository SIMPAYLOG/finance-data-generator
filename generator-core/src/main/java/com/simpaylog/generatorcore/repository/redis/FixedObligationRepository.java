package com.simpaylog.generatorcore.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.simpaylog.generatorcore.dto.FixedObligation;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FixedObligationRepository {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void saveAll(String sessionId, long userId, List<FixedObligation> items) {
        try {
            String key = getKey(sessionId, userId);
            String json = objectMapper.writeValueAsString(items);
            redisTemplate.opsForValue().set(key, json, Duration.ofHours(6));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FixedObligation> findAll(String sessionId, long userId) {
        String key = getKey(sessionId, userId);
        String json = redisTemplate.opsForValue().get(key);
        if(json == null || json.isEmpty()) {
            log.error("고정지출 데이터 없음 sessionId={}, userId={}", sessionId, userId);
            return List.of();
        }
        try {
            CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, FixedObligation.class);
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String sessionId, long userId) {
        redisTemplate.delete(getKey(sessionId, userId));
    }

    public boolean exists(String sessionId, long userId) {
        return redisTemplate.hasKey(getKey(sessionId, userId));
    }

    private String getKey(String sessionId, Long userId) {
        String key = sessionId;
        if (sessionId.isBlank()) key = "DEFAULT";
        return "fixed:" + key + ":" + userId;
    }
}
