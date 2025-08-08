package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.dto.analysis.AggregationInterval;
import com.simpaylog.generatorapi.dto.analysis.HourlyTransaction;
import com.simpaylog.generatorapi.dto.analysis.PeriodTransaction;
import com.simpaylog.generatorapi.dto.analysis.TimeHeatmapCell;
import com.simpaylog.generatorapi.dto.response.CommonChart;
import com.simpaylog.generatorapi.repository.Elasticsearch.TransactionAggregationRepository;
import com.simpaylog.generatorapi.utils.DateValidator;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.redis.RedisSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final TransactionAggregationRepository transactionAggregationRepository;
    private final RedisSessionRepository redisSessionRepository;

    public CommonChart<PeriodTransaction.PTSummary> searchByPeriod(String sessionId, LocalDate durationStart, LocalDate durationEnd, String interval) throws IOException {
        getSimulationSessionOrException(sessionId);
        DateValidator.validateDateRange(durationStart, durationEnd);
        AggregationInterval aggregationInterval = AggregationInterval.from(interval);
        PeriodTransaction result = transactionAggregationRepository.searchByPeriod(sessionId, durationStart, durationEnd, aggregationInterval);
        return switch (aggregationInterval) {
            case DAY -> new CommonChart<>("line", "일 별 트랜잭션 발생 금액", "날짜", "금액", result.results());
            case WEEK -> new CommonChart<>("line", "주 별 트랜잭션 발생 금액", "날짜", "금액", result.results());
            case MONTH -> new CommonChart<>("line", "월 별 트랜잭션 발생 금액", "날짜", "금액", result.results());
        };
    }

    public CommonChart<TimeHeatmapCell.TCSummary> searchTimeHeatmap(String sessionId, LocalDate durationStart, LocalDate durationEnd) throws IOException {
        getSimulationSessionOrException(sessionId);
        DateValidator.validateDateRange(durationStart, durationEnd);
        TimeHeatmapCell result = transactionAggregationRepository.searchTimeHeatmap(sessionId, durationStart, durationEnd);
        return new CommonChart<>("heatmap", "요일-시간대별 소비 건수", "시간대", "요일", result.results());

    }

    public CommonChart<HourlyTransaction.HourlySummary> searchTimeAmountAvgByPeriod(String sessionId, LocalDate durationStart, LocalDate durationEnd) throws IOException {
        getSimulationSessionOrException(sessionId);
        DateValidator.validateDateRange(durationStart, durationEnd);
        HourlyTransaction result = transactionAggregationRepository.searchByHour(sessionId, durationStart, durationEnd);
        return new CommonChart<>("line", "시간 별 트랜잭션 발생 금액 평균", "시간", "평균 금액", result.results());
    }

    private void getSimulationSessionOrException(String sessionId) {
        redisSessionRepository.find(sessionId).orElseThrow(() -> new CoreException(String.format("해당 sessionId를 찾을 수 없습니다. sessionId: %s", sessionId)));
    }


}
