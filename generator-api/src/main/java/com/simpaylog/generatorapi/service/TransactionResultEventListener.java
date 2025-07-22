package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.dto.TransactionResultEvent;
import com.simpaylog.generatorapi.dto.SimulationCompleteEvent;
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
    public void handleTransactionResult(TransactionResultEvent event) {
        log.info("Event received: {}", event.message());
        webSocketProgressService.sendProgressUpdate(event.message());
    }

    @Async
    @EventListener
    public void handleSimulationComplete(SimulationCompleteEvent event) {
        log.info("Simulation complete event received: {}", event.finalMessage());
        try {
            // 마지막 완료 메시지를 클라이언트로 전송
            webSocketProgressService.sendProgressUpdate(event.finalMessage());
            // 메시지가 확실히 전송되도록 잠시 대기
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // 모든 웹소켓 연결을 종료
            webSocketProgressService.closeAllSessions();
        }
    }
}