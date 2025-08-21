package com.simpaylog.generatorsimulator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

@Component
@RequiredArgsConstructor
public class ConcurrencyLimiter {
    private final Semaphore semaphore = new Semaphore(30);
    private final Executor virtualThreadExecutor;

    public void run(Runnable task) {
        virtualThreadExecutor.execute(() -> {
            try {
                semaphore.acquire();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        });
    }
}
