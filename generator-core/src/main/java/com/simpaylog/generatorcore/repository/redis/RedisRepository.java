package com.simpaylog.generatorcore.repository.redis;

import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, LocalDate> redisTemplate;

    private String getKey(String sessionId, Long userId, YearMonth yearMonth) {
        String key = sessionId;
        if (sessionId.isBlank()) key = "DEFAULT";
        return "payday:" + key + ":" + userId + ":" + yearMonth;
    }

    public void init(String sessionId, List<TransactionUserDto> users, LocalDate from, LocalDate to) {
        for (TransactionUserDto user : users) {
            for (LocalDate cur = from.withDayOfMonth(1); !cur.isAfter(to); cur = cur.plusMonths(1)) {
                redisTemplate.delete(getKey(sessionId, user.userId(), YearMonth.from(cur)));
            }
        }
    }

    public void register(String sessionId, Long userId, YearMonth yearMonth, List<LocalDate> paydays) {
        String key = getKey(sessionId, userId, yearMonth);
        if (redisTemplate.hasKey(key)) {
            log.warn("이미 데이터가 할당되어 있습니다: {}", key);
        }
        if (!paydays.isEmpty()) { // 빈 값은 Redis에 들어가지 않음
            redisTemplate.opsForSet().add(key, paydays.toArray(new LocalDate[0]));
            redisTemplate.expire(key, Duration.ofMinutes(5));
        }
    }

    public boolean isPayDay(String sessionId, Long userId, YearMonth yearMonth, LocalDate date) {
        String key = getKey(sessionId, userId, yearMonth);
        if (!redisTemplate.hasKey(key)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, date));
    }

    public int numberOfPayDays(String sessionId, Long userId, YearMonth yearMonth) {
        String key = getKey(sessionId, userId, yearMonth);
        Long size = redisTemplate.opsForSet().size(key);
        return size == null ? 0 : size.intValue();
    }

    public void clear(String sessionId, Long userId, YearMonth yearMonth) {
        redisTemplate.delete(getKey(sessionId, userId, yearMonth));
    }

}
