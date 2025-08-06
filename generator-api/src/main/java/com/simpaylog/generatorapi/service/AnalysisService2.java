package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.dto.analysis.HourlyTransaction;
import com.simpaylog.generatorapi.dto.response.CommonChart;
import com.simpaylog.generatorapi.repository.Elasticsearch.TransactionAggregationRepository2;
import com.simpaylog.generatorapi.utils.DateValidator;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.redis.RedisSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AnalysisService2 {
    private final TransactionAggregationRepository2 transactionAggregationRepository;
    private final RedisSessionRepository redisSessionRepository;

    public CommonChart<HourlyTransaction.HourlySummary> searchTimeSummaryByPeriod(String sessionId, LocalDate durationStart, LocalDate durationEnd) throws IOException {
//        getSimulationSessionOrException(sessionId);
        DateValidator.validateDateRange(durationStart, durationEnd);
        HourlyTransaction result = transactionAggregationRepository.searchByHour(sessionId, durationStart, durationEnd);
        return new CommonChart<>("line", "시간 별 트랜잭션 발생 금액 평균", "시간", "평균 금액", result.results());
    }



    private void getSimulationSessionOrException(String sessionId) {
        redisSessionRepository.find(sessionId).orElseThrow(() -> new CoreException(String.format("해당 sessionId를 찾을 수 없습니다. sessionId: %s", sessionId)));
    }


}
