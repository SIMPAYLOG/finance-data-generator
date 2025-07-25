package com.simpaylog.generatorapi.configuration.websocket;

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

    private final WebsocketSessionManager webSocketSessionManager;

    @Async
    @EventListener
    public void handleTransactionResult(TransactionResultResponse response) {
        log.info("Event received [{}]: {}", response.eventType(), response.message());
        webSocketSessionManager.sendProgressUpdate(response.message());

        if (response.eventType() == EventType.COMPLETE) {
            handleCompletion();
        }
    }

    private void handleCompletion() {
        try {
            Thread.sleep(100); // 메시지 전송 보장
        } catch (InterruptedException e) {
            log.error("Thread interrupted.", e);
            Thread.currentThread().interrupt();
        } finally {
            webSocketSessionManager.closeAllSessions();
        }
    }
}