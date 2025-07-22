package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.dto.enums.EventType;
import com.simpaylog.generatorapi.dto.response.TransactionResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionResultEventListener {

    private final WebSocketProgressService webSocketProgressService;

    @Async
    @EventListener
    public void handleTransactionResult(TransactionResultResponse response) {
        log.info("Event received [{}]: {}", response.eventType(), response.message());

        // 이벤트 타입에 따라 분기 처리
        if (response.eventType() == EventType.PROGRESS) {
            webSocketProgressService.sendProgressUpdate(response.message());

        } else if (response.eventType() == EventType.COMPLETE) {
            try {
                webSocketProgressService.sendProgressUpdate(response.message());
                Thread.sleep(100); // 메시지 전송 보장을 위한 대기
            } catch (InterruptedException e) {
                log.error("Thread interrupted.", e);
                Thread.currentThread().interrupt();
            } finally {
                webSocketProgressService.closeAllSessions();
            }
        }
    }
}