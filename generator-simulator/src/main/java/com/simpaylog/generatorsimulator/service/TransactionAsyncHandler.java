package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.dto.TransactionRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionAsyncHandler {
    private final TransactionService transactionService;
    private final ConcurrencyLimiter concurrencyLimiter;

    @Async("virtualThreadExecutor")
    public void handler(TransactionRequestEvent event) {
        try {
            concurrencyLimiter.run(() ->
                    transactionService.generateTransaction(event.transactionUserDto(), event.date())
            );
        } catch (Exception e) {
            e.printStackTrace();
            log.error("트랜잭션 처리 중 예외 발생 - userId={}, date={}", event.transactionUserDto().userId(), event.date());
        }
    }
}
