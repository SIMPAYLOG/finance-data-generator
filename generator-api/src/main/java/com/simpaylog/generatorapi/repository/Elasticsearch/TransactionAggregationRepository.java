package com.simpaylog.generatorapi.repository.Elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.analysis.*;
import com.simpaylog.generatorapi.utils.QueryBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class TransactionAggregationRepository {

    private final RestClient elasticsearchRestClient;
    private static final String ES_END_POINT = "/transaction-logs/_search";

    public PeriodTransaction searchByPeriod(String sessionId, LocalDate from, LocalDate to, AggregationInterval interval, Integer userId) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String queryJson = QueryBuilder.periodAggregationQuery(sessionId, from, to, interval.getCalendarInterval(), userId);
        request.setJsonEntity(queryJson);
        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);
        JsonNode buckets = root.path("aggregations").path("results").path("buckets");

        List<PeriodTransaction.PTSummary> results = new ArrayList<>();
        for (JsonNode bucket : buckets) {
            String date = bucket.path("key_as_string").asText();

            JsonNode totalSpent = bucket.path("total_spent");
            double spentAmount = totalSpent.path("amount_sum").path("value").asDouble(0.0);
            int spentCount = totalSpent.path("doc_count").asInt(0);

            JsonNode totalIncome = bucket.path("total_income");
            double incomeAmount = totalIncome.path("amount_sum").path("value").asDouble(0.0);
            int incomeCount = totalIncome.path("doc_count").asInt(0);

            results.add(new PeriodTransaction.PTSummary(date, spentCount, spentAmount, incomeCount, incomeAmount));
        }
        return new PeriodTransaction(interval, results);
    }

    public TimeHeatmapCell searchTimeHeatmap(String sessionId, LocalDate from, LocalDate to) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String queryJson = QueryBuilder.timeHeatmapQuery(sessionId, from, to);
        request.setJsonEntity(queryJson);
        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);
        JsonNode buckets = root.path("aggregations").path("by_day").path("buckets");

        List<TimeHeatmapCell.TCSummary> results = new ArrayList<>();
        for (JsonNode dayBucket : buckets) {
            int dayOfWeek = dayBucket.path("key").asInt() - 1;
            JsonNode hourBuckets = dayBucket.path("by_hour").path("buckets");

            for (JsonNode hourBucket : hourBuckets) {
                int hour = hourBucket.path("key").asInt(); // 0~23
                int count = hourBucket.path("doc_count").asInt();

                results.add(new TimeHeatmapCell.TCSummary(dayOfWeek, hour, count));
            }
        }
        return new TimeHeatmapCell(results);
    }

    public HourlyTransaction searchHourAmountAvg(String sessionId, LocalDate from, LocalDate to) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String queryJson = QueryBuilder.hourAggregationQuery(sessionId, from, to);
        request.setJsonEntity(queryJson);

        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);
        JsonNode buckets = root.path("aggregations").path("by_transaction_type").path("buckets");

        // 시간별 데이터 맵핑: hour -> HourlySummaryBuilder
        Map<Integer, HourlyTransaction.HourlySummary> hourlyMap = new HashMap<>();

        for (JsonNode typeBucket : buckets) {
            String transactionType = typeBucket.path("key").asText();
            JsonNode hourBuckets = typeBucket.path("by_hour").path("buckets");

            for (JsonNode hourBucket : hourBuckets) {
                int hour = hourBucket.path("key").asInt();
                int count = hourBucket.path("transaction_count").path("value").asInt(0);
                int avgAmount = hourBucket.path("average_amount").path("value").asInt(0);

                HourlyTransaction.HourlySummary existing = hourlyMap.getOrDefault(hour, new HourlyTransaction.HourlySummary(hour, 0, 0, 0, 0));

                HourlyTransaction.HourlySummary updated;
                if ("WITHDRAW".equalsIgnoreCase(transactionType)) {
                    updated = new HourlyTransaction.HourlySummary(hour, count, avgAmount, existing.totalIncomeCount(), existing.avgIncomeAmount());
                } else {
                    updated = new HourlyTransaction.HourlySummary(hour, existing.totalSpentCount(), existing.avgSpentAmount(), count, avgAmount);
                }

                hourlyMap.put(hour, updated);
            }
        }

        // 0~23시간까지 빠진 시간도 포함해서 정렬
        List<HourlyTransaction.HourlySummary> summaries = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            HourlyTransaction.HourlySummary hourlySummary = hourlyMap.getOrDefault(i, new HourlyTransaction.HourlySummary(0, 0, 0, 0, 0));
            summaries.add(hourlySummary);
        }

        return new HourlyTransaction(summaries);
    }

    public AmountAvgTransaction searchUserTradeAmountAvgByUserId(String sessionId, LocalDate from, LocalDate to, int userId) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String queryJson = QueryBuilder.userTradeAmountAvgQuery(sessionId, from, to, userId);
        request.setJsonEntity(queryJson);

        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);
        JsonNode buckets = root.path("aggregations").path("by_type").path("buckets");

        List<AmountAvgTransaction.AmountAvgTransactionSummary> summaries = new ArrayList<>();
        for (JsonNode bucket : buckets) {
            String transactionType = bucket.path("key").asText();
            int avgAmount = bucket.path("average_amount").path("value").asInt(0);
            summaries.add(new AmountAvgTransaction.AmountAvgTransactionSummary(transactionType, avgAmount));
        }

        return new AmountAvgTransaction(summaries);
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> parent, String key) {
        Object obj = parent.get(key);
        if (obj instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

}
