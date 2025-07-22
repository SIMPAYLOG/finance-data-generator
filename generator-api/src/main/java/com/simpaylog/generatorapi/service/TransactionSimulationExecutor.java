package com.simpaylog.generatorapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.TransactionResultEvent;
import com.simpaylog.generatorapi.dto.SimulationCompleteEvent;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import com.simpaylog.generatorsimulator.dto.DailyTransactionResult;
import com.simpaylog.generatorsimulator.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionSimulationExecutor {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;


    public void simulateTransaction(List<TransactionUserDto> users, LocalDate from, LocalDate to) {
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {

            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                List<DailyTransactionResult> dailyTransactionResults = processDailyTransactions(users, date, executor);
                processDailyResults(date, dailyTransactionResults);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("트랜잭션 시뮬레이션 중단", e);
        }  finally {
            eventPublisher.publishEvent(new SimulationCompleteEvent("모든 시뮬레이션이 완료되었습니다."));
        }
    }

    private List<DailyTransactionResult> processDailyTransactions(List<TransactionUserDto> users, LocalDate date, ExecutorService executor) throws InterruptedException {
        List<Callable<DailyTransactionResult>> tasks = new ArrayList<>();
        for (TransactionUserDto user : users) {
            tasks.add(() -> transactionService.generateTransaction(user, date));
        }

        List<Future<DailyTransactionResult>> futures = executor.invokeAll(tasks); // 하루치 병렬 실행
        List<DailyTransactionResult> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < users.size(); i++) {
            try {
                results.add(futures.get(i).get());
            } catch (ExecutionException e) {
                log.error("유저 처리 중 오류 [userId={}, date={}]: {}%n", users.get(i).userId(), date, e.getCause().getMessage());
            }
        }
        return results;
    }

    // TODO 클라이언트에서 작업 종료 기능 추가시 SessionId 추가 필요
    private void processDailyResults(LocalDate date, List<DailyTransactionResult> results) {
        try {
            String summaryMessage = String.format("==== %s 결과 요약 (총 %d건) ====", date, results.size());
            eventPublisher.publishEvent(new TransactionResultEvent(summaryMessage));

            for (DailyTransactionResult result : results) {
                String jsonResult = objectMapper.writeValueAsString(result);
                eventPublisher.publishEvent(new TransactionResultEvent(jsonResult));
            }
        } catch (Exception e) {
            log.error("Failed to publish result event", e);
        }
    }
}
