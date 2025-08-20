package com.simpaylog.generatorcore.repository.redis;

import com.simpaylog.generatorcore.session.SimulationSession;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisSessionRepository {

    @Resource(name = "simulationSessionRedisTemplate")
    private final RedisTemplate<String, SimulationSession> redisTemplate;
    private static final String PREFIX = "simulation:session:";

    public void save(SimulationSession session) {
        String key = PREFIX + session.sessionId();
        redisTemplate.opsForValue().set(key, session, Duration.ofHours(6));
    }

    public Optional<SimulationSession> find(String sessionId) {
        String key = PREFIX + sessionId;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public boolean hasKey(String sessionId) {
        String key = PREFIX + sessionId;
        return redisTemplate.hasKey(key);
    }

    public void delete(String sessionId) {
        redisTemplate.delete(PREFIX + sessionId);
    }

}
