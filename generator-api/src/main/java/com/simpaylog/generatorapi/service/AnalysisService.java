package com.simpaylog.generatorapi.service;

import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import com.simpaylog.generatorapi.dto.analysis.*;
import com.simpaylog.generatorapi.dto.chart.AgeGroupIncomeExpenseAverageDto;
import com.simpaylog.generatorapi.dto.chart.ChartIncomeCountDto;
import com.simpaylog.generatorapi.dto.chart.ChartIncomeIncomeExpenseDto;
import com.simpaylog.generatorapi.dto.response.CommonChart;
import com.simpaylog.generatorapi.exception.ApiException;
import com.simpaylog.generatorapi.exception.ErrorCode;
import com.simpaylog.generatorapi.repository.Elasticsearch.TransactionAggregationRepository;
import com.simpaylog.generatorapi.utils.DateValidator;
import com.simpaylog.generatorcore.dto.analyze.MinMaxDayDto;
import com.simpaylog.generatorcore.dto.analyze.UserAgeInfo;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.exception.CoreException;
import com.simpaylog.generatorcore.repository.UserRepository;
import com.simpaylog.generatorcore.repository.redis.RedisSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final TransactionAggregationRepository transactionAggregationRepository;
    private final RedisSessionRepository redisSessionRepository;
    private final UserRepository userRepository;

    public CommonChart<PeriodTransaction.PTSummary> searchByPeriod(String sessionId, LocalDate durationStart, LocalDate durationEnd, String interval, Integer userId) throws IOException {
        getSimulationSessionOrException(sessionId);
        DateValidator.validateDateRange(durationStart, durationEnd);
        AggregationInterval aggregationInterval = AggregationInterval.from(interval);
        PeriodTransaction result = transactionAggregationRepository.searchByPeriod(sessionId, durationStart, durationEnd, aggregationInterval, userId);
        return switch (aggregationInterval) {
            case DAY -> new CommonChart<>("line", "일 별 트랜잭션 발생 금액", "날짜", "금액", result.results());
            case WEEK -> new CommonChart<>("line", "주 별 트랜잭션 발생 금액", "날짜", "금액", result.results());
            case MONTH -> new CommonChart<>("line", "월 별 트랜잭션 발생 금액", "날짜", "금액", result.results());
        };
    }

    public CommonChart<PeriodTransaction.PTSummary> searchPeriodAmount(String sessionId, LocalDate durationStart, LocalDate durationEnd, String interval, Integer userId) throws IOException {
        getSimulationSessionOrException(sessionId);
        DateValidator.validateDateRange(durationStart, durationEnd);
        AggregationInterval aggregationInterval = AggregationInterval.from(interval);
        PeriodTransaction result = transactionAggregationRepository.searchPeriodAmount(sessionId, durationStart, durationEnd, aggregationInterval, userId);
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

    public CommonChart<HourlyTransaction.HourlySummary> searchHourAmountAvgByPeriod(String sessionId, LocalDate durationStart, LocalDate durationEnd, Integer userId) throws IOException {
        getSimulationSessionOrException(sessionId);
        DateValidator.validateDateRange(durationStart, durationEnd);
        HourlyTransaction result = transactionAggregationRepository.searchHourAmountAvg(sessionId, durationStart, durationEnd, userId);
        return new CommonChart<>("line", "시간 별 트랜잭션 발생 금액 평균", "시간", "평균 금액", result.results());
//        HourlyTransaction result = transactionAggregationRepository.searchHourAmountAvg(sessionId, durationStart, durationEnd, userId);
//        return result.results();
    }

    public CommonChart<AmountTransaction.AmountTransactionSummary> searchUserTradeAmountAvgByUserId(String sessionId, LocalDate durationStart, LocalDate durationEnd, Integer userId) throws IOException {
        getSimulationSessionOrException(sessionId);
        DateValidator.validateDateRange(durationStart, durationEnd);
        AmountTransaction result = transactionAggregationRepository.searchUserTradeAmountByUserId(sessionId, durationStart, durationEnd, userId);
        return new CommonChart<>("line", "수입/지출 금액 평균", "거래 종류", "총 금액", result.results());
    }

    public CommonChart<CategoryAmountTransaction.AmountTransactionSummary> searchUserCategoryTradeAmount(String sessionId, LocalDate durationStart, LocalDate durationEnd, Integer userId) throws IOException {
        getSimulationSessionOrException(sessionId);
        DateValidator.validateDateRange(durationStart, durationEnd);
        CategoryAmountTransaction result = transactionAggregationRepository.searchUserCategoryTradeAmount(sessionId, durationStart, durationEnd, userId);
        return new CommonChart<>("line", "카테고리별 수입/지출", "카테고리 종류", "총 금액", result.results());
    }

    private void getSimulationSessionOrException(String sessionId) {
        redisSessionRepository.find(sessionId).orElseThrow(() -> new CoreException(String.format("해당 sessionId를 찾을 수 없습니다. sessionId: %s", sessionId)));
    }

    public List<ChartIncomeIncomeExpenseDto> searchTransactionInfo(String sessionId, Optional<String> durationStartOpt, Optional<String> durationEndOpt, String type) {
        String durationStart = null;
        String durationEnd = null;
        String format;
        String interval;

        switch (type.toLowerCase()) {
            case "monthly":
                interval = CalendarInterval.Month.aliases()[0];
                format = "yyyy-MM";
                break;
            case "weekly":
                interval = CalendarInterval.Week.aliases()[0];
                format = "yyyy-MM-dd";
                break;
            case "daily":
            default:
                interval = CalendarInterval.Day.aliases()[0];
                format = "yyyy-MM-dd";
                break;
        }

        try {
            if (durationStartOpt.isEmpty() || durationEndOpt.isEmpty()) {
                MinMaxDayDto days = transactionAggregationRepository.saerchMinMaxDay(sessionId);
                durationStart = days.minDay();
                durationEnd = days.maxDay();
            } else {
                durationStart = durationStartOpt.get();
                durationEnd = durationEndOpt.get();
            }
            return transactionAggregationRepository.searchTransactionInfo(sessionId, durationStart, durationEnd, interval, format);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ApiException(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

    public Map<String, List<ChartIncomeCountDto>> searchCategoryByVomlumeTop5EachAgeGroup(String sessionId, String durationStart, String durationEnd) {
        // 분석할 연령대 목록 정의
        List<Integer> ageGroups = List.of(10, 20, 30, 40, 50, 60, 70);
        Map<String, List<ChartIncomeCountDto>> finalResults = new LinkedHashMap<>();

        for (Integer ageGroup : ageGroups) {
            List<Long> userIds = userRepository.findUserIdsByAgeGroup(ageGroup, sessionId);

            List<ChartIncomeCountDto> categoryDataForAgeGroup = new ArrayList<>();
            if (userIds != null && !userIds.isEmpty()) {
                try {
                    categoryDataForAgeGroup = transactionAggregationRepository.searchCategoryByVomlumeTop5EachAgeGroup(sessionId, userIds, durationStart, durationEnd);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new ApiException(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
                }
            }

            finalResults.put(ageGroup + "대", categoryDataForAgeGroup);
        }

        return finalResults;
    }

    public List<ChartIncomeCountDto> searchAllCategoryInfo(String sessionId, String durationStart, String durationEnd) {
        List<ChartIncomeCountDto> dataList;
        try {
            dataList = transactionAggregationRepository.searchAllCategoryInfo(sessionId, durationStart, durationEnd);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ApiException(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }

        return dataList;
    }

    public List<ChartIncomeCountDto> searchCategoryByVomlumeTop5(String sessionId, String durationStart, String durationEnd, Integer userId) {
        List<ChartIncomeCountDto> dataList;
        try {
            return transactionAggregationRepository.searchCategoryByVomlumeTop5(sessionId, durationStart, durationEnd, userId);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ApiException(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

    public Map<String, AgeGroupIncomeExpenseAverageDto> searchIncomeExpenseForAgeGroup(String sessionId, String durationStart, String durationEnd) {
        Map<Integer, List<Long>> idMap = new LinkedHashMap<>();
        for (int ageGroup = 10; ageGroup <= 70; ageGroup += 10) {
            idMap.put(ageGroup, new ArrayList<>());
        }

        Map<Integer, List<Long>> idMapFromDB = userRepository.findUserAgeInfoBySessionId(sessionId).stream()
                .collect(Collectors.groupingBy(
                        UserAgeInfo::age,
                        Collectors.mapping(UserAgeInfo::id, Collectors.toList())
                ));

        idMap.putAll(idMapFromDB);
        try {
            return transactionAggregationRepository.getFinancialsForGroup(sessionId, idMap, durationStart, durationEnd);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ApiException(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

    public Map<String, AgeGroupIncomeExpenseAverageDto> searchIncomeExpenseForPreferece(String sessionId, String durationStart, String durationEnd) {
        Map<Integer, List<Long>> idMap = new HashMap<>();
        for (Object[] row : userRepository.findUserIdsGroupedByPreferenceType(sessionId)) {
            int preferenceKey = PreferenceType.valueOf((String) row[0]).getKey();
            Long[] userIds = (Long[]) row[1];

            idMap.put(preferenceKey, Arrays.asList(userIds));
        }

        try {
            return transactionAggregationRepository.getFinancialsByPrefereceForGroup(sessionId, idMap, durationStart, durationEnd);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ApiException(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

    public IncomeExpenseDto searchIncomeExpense(String sessionId, String durationStart, String durationEnd, Integer userId) {
        try {
            return transactionAggregationRepository.saerchIncomeExpense(sessionId, durationStart, durationEnd, userId);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ApiException(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
    }

}
