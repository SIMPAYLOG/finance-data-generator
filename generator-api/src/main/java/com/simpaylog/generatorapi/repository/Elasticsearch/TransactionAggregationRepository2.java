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
        Map<Integer, HourlySummary> hourlyMap = new HashMap<>();

        for (JsonNode typeBucket : buckets) {
            String transactionType = typeBucket.path("key").asText();
            JsonNode hourBuckets = typeBucket.path("by_hour").path("buckets");

            for (JsonNode hourBucket : hourBuckets) {
                int hour = hourBucket.path("key").asInt();
                int count = hourBucket.path("transaction_count").path("value").asInt(0);
                int avgAmount = hourBucket.path("average_amount").path("value").asInt(0);

                HourlySummary existing = hourlyMap.getOrDefault(hour, new HourlySummary(hour, 0, 0, 0, 0));

                HourlySummary updated;
                if ("WITHDRAW".equalsIgnoreCase(transactionType)) {
                    updated = new HourlySummary(hour, count, avgAmount, existing.totalIncomeCount(), existing.avgIncomeAmount());
                } else {
                    updated = new HourlySummary(hour, existing.totalSpentCount(), existing.avgSpentAmount(), count, avgAmount);
                }

                hourlyMap.put(hour, updated);
            }
        }

        // 0~23시간까지 빠진 시간도 포함해서 정렬
        List<HourlySummary> summaries = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            HourlySummary hourlySummary = hourlyMap.getOrDefault(i, new HourlySummary(0, 0, 0, 0, 0));
            summaries.add(hourlySummary);
        }

        return new HourlyTransaction(summaries);
    }
}
