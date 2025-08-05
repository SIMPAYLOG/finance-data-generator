package com.simpaylog.generatorapi.service;

import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import com.simpaylog.generatorapi.dto.chart.ChartCategoryDto;
import com.simpaylog.generatorapi.dto.response.ChartResponse;
import com.simpaylog.generatorapi.repository.Elasticsearch.ElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionLogService {
    private final ElasticsearchRepository elasticsearchRepository;

    public ChartResponse getCategoryCounts() throws IOException {
        List<ChartCategoryDto> dataList = elasticsearchRepository.categorySumary();

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
}