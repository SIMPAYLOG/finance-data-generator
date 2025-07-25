package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.dto.enums.EventType;
import com.simpaylog.generatorapi.dto.response.TransactionResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class TransactionProgressTracker {
    private final Map<String, AtomicInteger> progressMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> expectedCounts = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    public void initProgress(String sessionId, int expected) {
        eventPublisher.publishEvent(new TransactionResultResponse("트랜잭션 데이터 로그 생성을 시작합니다.", EventType.START));
        String key = generateKey(sessionId);
        progressMap.put(key, new AtomicInteger(0));
        expectedCounts.put(key, expected);
        eventPublisher.publishEvent(new TransactionResultResponse(String.format("진행 중 %f%%", 0.0), EventType.PROGRESS));
    }

    public void increment(String sessionId) {
        String key = generateKey(sessionId);
        AtomicInteger counter = progressMap.get(key);
        int current = counter.incrementAndGet();
        int total = expectedCounts.get(key);

        double percent = (double) current / total * 100;
        long roundedPercentage = Math.round(percent);

        eventPublisher.publishEvent(new TransactionResultResponse(String.format("진행 중 %d%%", roundedPercentage), EventType.PROGRESS));
        if(Double.compare(percent, 100.0) == 0) {
            eventPublisher.publishEvent(new TransactionResultResponse("시뮬레이션이 정상적으로 종료되었습니다.", EventType.COMPLETE));
        }
    }

    private String generateKey(String sessionId) {
        if(sessionId == null || sessionId.isBlank()) {
            return "DEFAULT_KEY";
        }
        return sessionId;
    }
}
