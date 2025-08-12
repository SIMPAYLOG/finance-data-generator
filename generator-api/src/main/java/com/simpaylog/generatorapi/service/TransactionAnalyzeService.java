package com.simpaylog.generatorapi.service;

import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import com.simpaylog.generatorapi.dto.chart.*;
import com.simpaylog.generatorapi.dto.response.ChartResponse;
import com.simpaylog.generatorapi.repository.Elasticsearch.ElasticsearchRepository;
import com.simpaylog.generatorcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransactionAnalyzeService {
    private final ElasticsearchRepository elasticsearchRepository;
    private final UserRepository userRepository;

    public ChartResponse getCategoryCounts(String sessionId) throws IOException {
        List<ChartCategoryDto> dataList = elasticsearchRepository.categorySumary(sessionId);

        return new ChartResponse("bar", "카테고리별 거래량", "카테고리", "거래건수", dataList);
    }

    public ChartResponse getTopVomlumeCategoryCounts(String sessionId, String durationStart, String durationEnd) throws IOException {
        List<ChartCategoryDto> dataList = elasticsearchRepository.topVolumeCategorySumary(sessionId, durationStart, durationEnd);

        return new ChartResponse("bar", "카테고리별 거래량", "카테고리", "거래건수", dataList);
    }

    public ChartResponse getTransactionSummary(String durationStart, String durationEnd, String type, String sessionId) throws IOException {
        final String title;
        final CalendarInterval interval;
        final DateTimeFormatter formatter;
        final String typeStr;

        switch (type.toLowerCase()) {
            case "monthly":
                title = "월별 거래 요약";
                interval = CalendarInterval.Month;
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                typeStr = "yyyy-MM";
                break;
            case "weekly":
                title = "주간 거래 요약";
                interval = CalendarInterval.Week;
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                typeStr = "yyyy-MM-dd";
                break;
            case "daily":
            default:
                title = "일별 거래 요약";
                interval = CalendarInterval.Day;
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                typeStr = "yyyy-MM-dd";
                break;
        }

        List<ChartCategoryDto> dataList = elasticsearchRepository.getTransactionSummary(durationStart, durationEnd,type, interval, formatter, typeStr, sessionId);

        return new ChartResponse("line", title, "날짜", "거래금액", dataList);
    }

    public Map<String, List<ChartData>> getCategorySummaryByAllAgeGroups(String sessionId) throws IOException {
        // 1. 분석할 연령대 목록 정의
        List<Integer> ageGroups = List.of(10, 20, 30, 40, 50, 60, 70);
        // 2. 최종 결과를 담을 Map 생성
        Map<String, List<ChartData>> finalResults = new LinkedHashMap<>();

        for (Integer age : ageGroups) {
            List<Long> userIds = userRepository.findUserIdsByAgeGroup(age, sessionId);

            List<ChartData> categoryDataForAgeGroup = new ArrayList<>();
            if (userIds != null && !userIds.isEmpty()) {
                categoryDataForAgeGroup = elasticsearchRepository.getCategorySummaryByAgeGroup(userIds);
            }

            finalResults.put(age + "대", categoryDataForAgeGroup);
        }

        return finalResults;
    }

    public Map<String, AgeGroupIncomeExpenseAverageDto> getFinancialsForGroup(String sessionId, Map<Integer, List<Long>> idMap, String durationStart, String durationEnd) throws IOException {
        return elasticsearchRepository.getFinancialsForGroup(sessionId, idMap, durationStart, durationEnd);
    }

    public Map<String, AgeGroupIncomeExpenseAverageDto> getFinancialsByPrefereceForGroup(String sessionId, Map<Integer, List<Long>> idMap, String durationStart, String durationEnd) throws IOException {
        return elasticsearchRepository.getFinancialsByPrefereceForGroup(sessionId, idMap, durationStart, durationEnd);
    }

}