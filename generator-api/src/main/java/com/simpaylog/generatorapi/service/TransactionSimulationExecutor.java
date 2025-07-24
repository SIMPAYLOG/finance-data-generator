package com.simpaylog.generatorapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.enums.EventType;
import com.simpaylog.generatorapi.dto.response.TransactionResultResponse;
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
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                List<DailyTransactionResult> dailyTransactionResults = processDailyTransactions(users, date, executor);
                processDailyResults(date, dailyTransactionResults);
            }
            eventPublisher.publishEvent(new TransactionResultResponse("시뮬레이션이 정상적으로 종료되었습니다.", EventType.COMPLETE));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            eventPublisher.publishEvent(new TransactionResultResponse("예기치 못한 에러로 작업이 중단되었습니다.", EventType.FAIL));
            log.error("트랜잭션 시뮬레이션 중단 : {}", e.getCause().getMessage());
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
    private void processDailyResults(LocalDate date, List<DailyTransactionResult> results){
        if(results.isEmpty()){
            return;
        }
        String summaryMessage = String.format("==== %s 결과 요약 (총 %d건) ====", date, results.size());
        eventPublisher.publishEvent(new TransactionResultResponse(summaryMessage, EventType.PROGRESS));

        try{
            for (DailyTransactionResult result : results) {
                String jsonResult = objectMapper.writeValueAsString(result);
                eventPublisher.publishEvent(new TransactionResultResponse(jsonResult, EventType.PROGRESS));
            }
        } catch (JsonProcessingException e) {
            log.error("error occurred while parsing json result: {}", e.getMessage());
        }
    }
}
