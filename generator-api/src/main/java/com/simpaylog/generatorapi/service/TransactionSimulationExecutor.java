package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorsimulator.dto.DailyTransactionResult;
import com.simpaylog.generatorsimulator.service.TransactionService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class TransactionSimulationExecutor {

    private final TransactionService transactionService;

    public void simulateTransaction(List<User> users, LocalDate from, LocalDate to) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                List<Callable<DailyTransactionResult>> tasks = new ArrayList<>();

                for (User user : users) {
                    LocalDate finalDate = date;
                    tasks.add(() -> transactionService.generateTransaction(TransactionUserDto.fromEntity(user), finalDate));
                }
                List<Future<DailyTransactionResult>> futures = executor.invokeAll(tasks); // 하루치 병렬 실행
                List<DailyTransactionResult> results = Collections.synchronizedList(new ArrayList<>());

                for (Future<DailyTransactionResult> future : futures) {
                    try {
                        results.add(future.get());
                    } catch (ExecutionException e) {
                        System.err.println("유저 처리 중 오류: " + e.getCause().getMessage());
                    }
                }
            }

            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("트랜잭션 시뮬레이션 중단", e);
        }
    }

    private void processDailyResults(LocalDate date, List<DailyTransactionResult> results) {
        System.out.println("==== " + date + " 결과 요약 ====");
        for (DailyTransactionResult result : results) {
            System.out.printf("[%s] userId: %d, income: %d (%d회), spending: %d (%d회)\n",
                    date,
                    result.userId(),
                    result.totalIncome(),
                    result.incomeTransactionCount(),
                    result.totalSpending(),
                    result.spendingTransactionCount()
            );
        }
    }
}
