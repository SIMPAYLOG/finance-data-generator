package com.simpaylog.generatorapi.repository.Elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.analysis.HourlyTransaction;
import com.simpaylog.generatorapi.dto.analysis.HourlyTransaction.HourlySummary;
import com.simpaylog.generatorapi.utils.QueryBuilder2;
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
public class TransactionAggregationRepository2 {

    private final RestClient elasticsearchRestClient;
    private static final String ES_END_POINT = "/transaction-logs/_search";

    public HourlyTransaction searchByHour(String sessionId, LocalDate from, LocalDate to) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String queryJson = QueryBuilder2.hourAggregationQuery(sessionId, from, to);
        request.setJsonEntity(queryJson);

        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);
        JsonNode buckets = root.path("aggregations").path("by_transaction_type").path("buckets");

        // 시간별 데이터 맵핑: hour -> HourlySummaryBuilder
        Map<Integer, HourlySummaryBuilder> hourlyMap = new HashMap<>();

        for (JsonNode typeBucket : buckets) {
            String transactionType = typeBucket.path("key").asText(); // "WITHDRAW" or "DEPOSIT"
            JsonNode hourBuckets = typeBucket.path("by_hour").path("buckets");

            for (JsonNode hourBucket : hourBuckets) {
                int hour = hourBucket.path("key").asInt();
                int count = hourBucket.path("transaction_count").path("value").asInt();
                double avgAmount = hourBucket.path("average_amount").path("value").asDouble(0.0);

                HourlySummaryBuilder builder = hourlyMap.getOrDefault(hour, new HourlySummaryBuilder(hour));

                if ("WITHDRAW".equalsIgnoreCase(transactionType)) {
                    builder.totalSpentCount = count;
                    builder.avgSpentAmount = avgAmount;
                } else if ("DEPOSIT".equalsIgnoreCase(transactionType)) {
                    builder.totalIncomeCount = count;
                    builder.avgIncomeAmount = avgAmount;
                }

                hourlyMap.put(hour, builder);
            }
        }

        // 0~23시간까지 빠진 시간도 포함해서 정렬
        List<HourlySummary> summaries = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            HourlySummaryBuilder builder = hourlyMap.getOrDefault(i, new HourlySummaryBuilder(i));
            summaries.add(builder.build());
        }

        return new HourlyTransaction(summaries);
    }

    private static class HourlySummaryBuilder {
        int hour;
        int totalSpentCount = 0;
        double avgSpentAmount = 0.0;
        int totalIncomeCount = 0;
        double avgIncomeAmount = 0.0;

        public HourlySummaryBuilder(int hour) {
            this.hour = hour;
        }

        public HourlySummary build() {
            return new HourlySummary(hour, totalSpentCount, avgSpentAmount, totalIncomeCount, avgIncomeAmount);
        }
    }
}
