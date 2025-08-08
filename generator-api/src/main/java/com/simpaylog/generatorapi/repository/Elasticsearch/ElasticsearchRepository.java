package com.simpaylog.generatorapi.repository.Elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.NamedValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.chart.AgeGroupIncomeExpenseAverageDto;
import com.simpaylog.generatorapi.dto.chart.ChartCategoryDto;
import com.simpaylog.generatorapi.dto.chart.ChartData;
import com.simpaylog.generatorapi.dto.document.TransactionLogDocument;
import com.simpaylog.generatorapi.utils.QueryBuilder;
import com.simpaylog.generatorcore.exception.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ElasticsearchRepository {

    private final ElasticsearchClient client;
    private final RestClient elasticsearchRestClient;
    private static final String ES_END_POINT = "/transaction-logs/_search";

    public void findAllTransactionsForExport(String sessionId, Consumer<TransactionLogDocument> consumer) {
        List<FieldValue> searchAfter = null;
        try {
            while (true) {
                SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                        .index("transaction-logs")
                        .size(1000)
                        .sort(s -> s.field(f -> f.field("userId").order(SortOrder.Asc)))
                        .sort(s -> s.field(f -> f.field("timestamp").order(SortOrder.Asc)))
                        .sort(s -> s.field(f -> f.field("uuid").order(SortOrder.Asc)))  // _id 추가
                        .query(q -> q.term(t -> t.field("sessionId").value(sessionId)));

                if (searchAfter != null) {
                    searchBuilder.searchAfter(searchAfter);
                }

                SearchResponse<TransactionLogDocument> response = client.search(
                        searchBuilder.build(),
                        TransactionLogDocument.class
                );

                List<Hit<TransactionLogDocument>> hits = response.hits().hits();
                if (hits.isEmpty()) break;

                for (Hit<TransactionLogDocument> hit : hits) {
                    consumer.accept(hit.source());
                }

                List<FieldValue> lastSort = hits.get(hits.size() - 1).sort();
                if (lastSort.equals(searchAfter)) {  // 이전과 같으면 무한루프 방지
                    break;
                }
                searchAfter = lastSort;
            }
        } catch (IOException e) {
            log.error("IOException : {}", e.getMessage());
            throw new CoreException("Elasticsearch 통신 중 IOException 발생");
        } catch (Exception e) {
            log.error("Exception : {}", e.getMessage());
            throw new CoreException("Elasticsearch 조회 중 알 수 없는 예외 발생");
        }
    }
    public List<ChartCategoryDto> categorySumary(String sessionId) throws IOException {
        return client.search(s -> s
                                .index("transaction-logs")
                                .size(0)
                                .query(q -> q
                                        .term(t -> t
                                                .field("sessionId")
                                                .value(sessionId)
                                        )
                                )
                                .aggregations("category_count", a -> a
                                        .terms(t -> t
                                                .field("category")
                                        )
                                        .aggregations("total_amount", sa -> sa
                                                .sum(v -> v
                                                        .script(sc -> sc
                                                                .source("doc.containsKey('amount') ? doc['amount'].value : 0")
                                                        )
                                                )
                                        )
                                ),
                        Void.class
                ).aggregations().get("category_count").sterms().buckets().array().stream()
                .map(b -> new ChartCategoryDto(
                        b.key().stringValue(),
                        (long) b.aggregations().get("total_amount").sum().value(),
                        b.docCount()
                ))
                .collect(Collectors.toList());
    }

    public List<ChartCategoryDto> topVolumeCategorySumary(String sessionId, String durationStart, String durationEnd) throws IOException {
        SearchResponse<Void> response = client.search(s -> s
                        .index("transaction-logs")
                        .size(0)
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m
                                                .term(t -> t
                                                        .field("sessionId")
                                                        .value(sessionId)
                                                )
                                        )
                                        .must(m -> m
                                                .range(r -> r
                                                        .date(d -> d
                                                                .field("timestamp")
                                                                .from(durationStart)
                                                                .to(durationEnd)
                                                                .timeZone("Asia/Seoul")
                                                        )
                                                )
                                        )
                                )
                        )
                        .aggregations("category_count", a -> a
                                .terms(t -> t
                                        .field("category")
                                        .size(5)
                                        .order(NamedValue.of("total_amount", SortOrder.Desc))
                                )
                                .aggregations("total_amount", sa -> sa
                                        .sum(v -> v
                                                .script(sc -> sc
                                                        .source("doc.containsKey('amount') ? doc['amount'].value : 0")
                                                )
                                        )
                                )
                        ),
                Void.class
        );

        return response.aggregations().get("category_count").sterms().buckets().array().stream()
                .map(b -> new ChartCategoryDto(
                        b.key().stringValue(),
                        (long) b.aggregations().get("total_amount").sum().value(),
                        b.docCount()
                ))
                .collect(Collectors.toList());
    }

    public List<ChartCategoryDto> getTransactionSummary(String start, String end, String intervalType, CalendarInterval interval, DateTimeFormatter formatter, String typeStr, String sessionId) throws IOException {
        SearchResponse<Void> response = client.search(s -> s
                        .index("transaction-logs")
                        .size(0)
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m
                                                .range(r -> r
                                                        .date(d -> d
                                                                .field("timestamp")
                                                                .from(start)
                                                                .to(end)
                                                                .timeZone("Asia/Seoul")
                                                        )
                                                )
                                        )
                                        .must(m -> m
                                                .term(t -> t
                                                        .field("sessionId")
                                                        .value(sessionId)
                                                )
                                        )
                                )
                        )
                        .aggregations("summary", aggBuilder -> aggBuilder
                                .dateHistogram(histBuilder -> histBuilder
                                        .field("timestamp")
                                        .calendarInterval(interval)
                                        .timeZone("Asia/Seoul")
                                        .format(typeStr)
                                )
                                .aggregations("total_amount", subAggBuilder -> subAggBuilder
                                        .sum(sumBuilder -> sumBuilder
                                                .field("amount")
                                        )
                                )
                        ),
                Void.class
        );

        Map<String, ChartCategoryDto> resultsMap = response.aggregations().get("summary").dateHistogram().buckets().array().stream()
                .collect(Collectors.toMap(
                        DateHistogramBucket::keyAsString,
                        b -> new ChartCategoryDto(
                                b.keyAsString(),
                                (long) b.aggregations().get("total_amount").sum().value(),
                                b.docCount()
                        )
                ));

        List<ChartCategoryDto> completeData = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);

        LocalDate currentDate;
        // 시작 날짜를 각 interval의 시작점으로 정렬
        switch (intervalType.toLowerCase()) {
            case "monthly":
                currentDate = startDate.withDayOfMonth(1);
                break;
            case "weekly":
                currentDate = startDate.with(java.time.DayOfWeek.MONDAY);
                break;
            default: // daily
                currentDate = startDate;
                break;
        }

        while (!currentDate.isAfter(endDate)) {
            String dateKey = currentDate.format(formatter);
            ChartCategoryDto chartData = resultsMap.getOrDefault(dateKey, new ChartCategoryDto(dateKey, 0L, 0L));
            completeData.add(chartData);

            // 다음 날짜/주/월로 이동
            switch (intervalType.toLowerCase()) {
                case "monthly":
                    currentDate = currentDate.plusMonths(1);
                    break;
                case "weekly":
                    currentDate = currentDate.plusWeeks(1);
                    break;
                default: // daily
                    currentDate = currentDate.plusDays(1);
                    break;
            }
        }
        return completeData;
    }

    public List<ChartData> getCategorySummaryByAgeGroup(List<Long> userIds) throws IOException {
        List<FieldValue> userIdFieldValues = userIds.stream()
                .map(FieldValue::of)
                .collect(Collectors.toList());

        SearchResponse<Void> response = client.search(s -> s
                        .index("transaction-logs")
                        .size(0)
                        .query(q -> q
                                .terms(t -> t
                                        .field("userId")
                                        .terms(tv -> tv.value(userIdFieldValues))
                                )
                        )
                        .aggregations("category_count", a -> a
                                .terms(t -> t
                                        .field("category")
                                        .size(5)
                                        .order(List.of(
                                                NamedValue.of("total_amount", SortOrder.Desc)
                                        ))
                                )
                                .aggregations("total_amount", sa -> sa
                                        .sum(v -> v.script(sc -> sc.source("doc.containsKey('amount') ? doc['amount'].value : 0")))

                                )
                                .aggregations("total_amount", sa -> sa
                                        .sum(v -> v.script(sc -> sc.source("doc.containsKey('amount') ? doc['amount'].value : 0")))
                                )
                        ),
                Void.class
        );

        return response.aggregations().get("category_count").sterms().buckets().array().stream()
                .map(b -> new ChartData(
                        b.key().stringValue(),
                        (long) b.aggregations().get("total_amount").sum().value(),
                        b.docCount()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, AgeGroupIncomeExpenseAverageDto> getFinancialsForGroup (String sessionId, Map<Integer, List<Long>> userIds, String durationStart, String durationEnd) throws IOException {
        Map<String, AgeGroupIncomeExpenseAverageDto> finalResults = new LinkedHashMap<>();
        Request request = new Request("GET", ES_END_POINT);

        request.setJsonEntity(QueryBuilder.incomeExpenseByAgeGroupQuery(sessionId, userIds, durationStart, durationEnd));
        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);
        JsonNode buckets = root.path("aggregations").path("age_group_summary").path("buckets");

        for (int i = 10; i <= 70; i += 10) {
            String ageGroupKey = i + "대";

            JsonNode currentAgeBucket = buckets.path(ageGroupKey);

            long avgIncome = 0;
            long avgExpense = 0;

            if (!currentAgeBucket.isMissingNode()) {
                avgIncome = currentAgeBucket
                        .path("income_expense_split")
                        .path("buckets")
                        .path("income")
                        .path("average_per_user")
                        .path("value")
                        .asLong(0);

                avgExpense = currentAgeBucket
                        .path("income_expense_split")
                        .path("buckets")
                        .path("expense")
                        .path("average_per_user")
                        .path("value")
                        .asLong(0);
            }
            finalResults.put(ageGroupKey, new AgeGroupIncomeExpenseAverageDto(avgIncome, avgExpense));
        }

        return finalResults;
    }

}