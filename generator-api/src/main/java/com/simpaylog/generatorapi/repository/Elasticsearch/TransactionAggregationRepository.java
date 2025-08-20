package com.simpaylog.generatorapi.repository.Elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.MaxAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.MinAggregate;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.NamedValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorapi.dto.analysis.*;
import com.simpaylog.generatorapi.dto.chart.AgeGroupIncomeExpenseAverageDto;
import com.simpaylog.generatorapi.dto.chart.ChartIncomeCountDto;
import com.simpaylog.generatorapi.dto.chart.ChartIncomeIncomeExpenseDto;
import com.simpaylog.generatorapi.dto.document.TransactionLogDocument;
import com.simpaylog.generatorapi.utils.QueryBuilder;
import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorcore.dto.analyze.MinMaxDayDto;
import com.simpaylog.generatorcore.enums.PreferenceType;
import com.simpaylog.generatorcore.exception.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TransactionAggregationRepository {

    private final ElasticsearchClient elasticsearchClient;
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

    public PeriodTransaction searchPeriodAmount(String sessionId, LocalDate from, LocalDate to, AggregationInterval interval, Integer userId) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String queryJson = QueryBuilder.periodAmountQuery(sessionId, from, to, interval.getCalendarInterval(), userId);
        request.setJsonEntity(queryJson);
        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);
        JsonNode buckets = root.path("aggregations").path("results").path("buckets");

        List<PeriodTransaction.PTSummary> results = new ArrayList<>();
        for (JsonNode bucket : buckets) {
            String date = bucket.path("key_as_string").asText();

            int totalSpentCount;
            double spentAmountSum;
            int totalIncomeCount;
            double incomeAmountSum;

            if (userId != null) {
                // userId가 있는 경우: 기존 방식
                totalSpentCount = bucket.path("total_spent").path("doc_count").asInt(0);
                spentAmountSum = bucket.path("total_spent").path("amount_sum").path("value").asDouble(0.0);

                totalIncomeCount = bucket.path("total_income").path("doc_count").asInt(0);
                incomeAmountSum = bucket.path("total_income").path("amount_sum").path("value").asDouble(0.0);
            } else {
                // userId 없는 경우: avg_bucket 기준
                totalSpentCount = bucket.path("total_spent_sum").path("doc_count").asInt(0);
                spentAmountSum = bucket.path("total_spent").path("value").asDouble(0.0);

                totalIncomeCount = bucket.path("total_income_sum").path("doc_count").asInt(0);
                incomeAmountSum = bucket.path("total_income").path("value").asDouble(0.0);
            }

            results.add(new PeriodTransaction.PTSummary(date, totalSpentCount, spentAmountSum, totalIncomeCount, incomeAmountSum));
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

    public HourlyTransaction searchHourAmountAvg(String sessionId, LocalDate from, LocalDate to, Integer userId) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String queryJson = QueryBuilder.hourAggregationQuery(sessionId, from, to, userId);
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
            HourlyTransaction.HourlySummary hourlySummary = hourlyMap.getOrDefault(i, new HourlyTransaction.HourlySummary(i, 0, 0, 0, 0));
            summaries.add(hourlySummary);
        }

        return new HourlyTransaction(summaries);
    }

    public AmountTransaction searchUserTradeAmountByUserId(String sessionId, LocalDate from, LocalDate to, Integer userId) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String queryJson = QueryBuilder.userTradeAmountQuery(sessionId, from, to, userId);
        request.setJsonEntity(queryJson);

        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);
        JsonNode buckets = root.path("aggregations").path("by_type").path("buckets");

        List<AmountTransaction.AmountTransactionSummary> summaries = new ArrayList<>();
        for (JsonNode bucket : buckets) {
            String transactionType = bucket.path("key").asText();
            int avgAmount = bucket.path("total_amount").path("value").asInt(0);
            summaries.add(new AmountTransaction.AmountTransactionSummary(transactionType, avgAmount));
        }

        return new AmountTransaction(summaries);
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> parent, String key) {
        Object obj = parent.get(key);
        if (obj instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    private AgeGroupIncomeExpenseAverageDto parseFinancialsFromBucket(JsonNode bucketNode) {
        if (bucketNode.isMissingNode()) {
            return new AgeGroupIncomeExpenseAverageDto(0L, 0L);
        }

        long avgIncome = bucketNode
                .path("income_expense_split")
                .path("buckets")
                .path("income")
                .path("average_per_user")
                .path("value")
                .asLong(0);

        long avgExpense = bucketNode
                .path("income_expense_split")
                .path("buckets")
                .path("expense")
                .path("average_per_user")
                .path("value")
                .asLong(0);

        return new AgeGroupIncomeExpenseAverageDto(avgIncome, avgExpense);
    }

    public Map<String, AgeGroupIncomeExpenseAverageDto> getFinancialsForGroup(String sessionId, Map<Integer, List<Long>> userIds, String durationStart, String durationEnd) throws IOException {
        Map<String, AgeGroupIncomeExpenseAverageDto> finalResults = new LinkedHashMap<>();
        JsonNode buckets = executeAndGetBucketsForFinancial(sessionId, userIds, durationStart, durationEnd);

        for (int i = 10; i <= 70; i += 10) {
            String ageGroupKey = Integer.toString(i);
            JsonNode currentAgeBucket = buckets.path(ageGroupKey);

            AgeGroupIncomeExpenseAverageDto dto = parseFinancialsFromBucket(currentAgeBucket);
            finalResults.put(ageGroupKey + "대", dto);
        }

        return finalResults;
    }

    public Map<String, AgeGroupIncomeExpenseAverageDto> getFinancialsByPrefereceForGroup(String sessionId, Map<Integer, List<Long>> userIds, String durationStart, String durationEnd) throws IOException {
        Map<String, AgeGroupIncomeExpenseAverageDto> finalResults = new LinkedHashMap<>();
        JsonNode buckets = executeAndGetBucketsForFinancial(sessionId, userIds, durationStart, durationEnd);

        for (int key : Arrays.stream(PreferenceType.values())
                .filter(type -> type != PreferenceType.DEFAULT)
                .map(PreferenceType::getKey)
                .collect(Collectors.toList())) {
            JsonNode currentAgeBucket = buckets.path(Integer.toString(key));

            AgeGroupIncomeExpenseAverageDto dto = parseFinancialsFromBucket(currentAgeBucket);
            finalResults.put(PreferenceType.fromKey(key).getName(), dto);
        }

        return finalResults;
    }

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

                SearchResponse<TransactionLogDocument> response = elasticsearchClient.search(
                        searchBuilder.build(),
                        TransactionLogDocument.class
                );

                List<Hit<TransactionLogDocument>> hits = response.hits().hits();
                if (hits.isEmpty()) break;

                for (Hit<TransactionLogDocument> hit : hits) {
                    consumer.accept(hit.source());
                }

                List<FieldValue> lastSort = hits.get(hits.size() - 1).sort();
                if (lastSort.equals(searchAfter)) {
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

    public List<ChartIncomeCountDto> searchAllCategoryInfo(
            String sessionId,
            String durationStart,
            String durationEnd,
            Integer userId
    ) throws IOException {
        return elasticsearchClient.search(s -> s
                                .index("transaction-logs")
                                .size(0)
                                .query(q -> q
                                        .bool(b -> {
                                            // 필수 조건: sessionId
                                            b.must(m -> m.term(t -> t.field("sessionId").value(sessionId)));
                                            // 필수 조건: timestamp 범위
                                            b.must(m -> m.range(r -> r.date(d -> d.field("timestamp")
                                                    .from(durationStart)
                                                    .to(durationEnd)
                                                    .timeZone("Asia/Seoul")
                                            )));
                                            // 필수 조건: WITHDRAW
                                            b.must(m -> m.term(t -> t.field("transactionType").value("WITHDRAW")));

                                            // 선택 조건: userId
                                            if (userId != null) {
                                                b.must(m -> m.term(t -> t.field("userId").value(userId)));
                                            }
                                            return b;
                                        })
                                )
                                .aggregations("category_count", a -> a
                                        .terms(t -> t.field("category"))
                                        .aggregations("total_amount", sa -> sa
                                                .sum(v -> v.script(sc -> sc
                                                        .source("doc.containsKey('amount') ? doc['amount'].value : 0")
                                                ))
                                        )
                                ),
                        Void.class
                ).aggregations().get("category_count").sterms().buckets().array().stream()
                .map(b -> new ChartIncomeCountDto(
                        CategoryType.fromKey(b.key().stringValue()).getLabel(),
                        (long) b.aggregations().get("total_amount").sum().value(),
                        b.docCount()
                ))
                .sorted(Comparator.comparing(ChartIncomeCountDto::income).reversed())
                .collect(Collectors.toList());
    }


    public List<ChartIncomeCountDto> searchCategoryByVomlumeTop5(
            String sessionId,
            String durationStart,
            String durationEnd,
            Integer userId
    ) throws IOException {
        SearchResponse<Void> response = elasticsearchClient.search(s -> s
                        .index("transaction-logs")
                        .size(0)
                        .query(q -> q
                                .bool(b -> {
                                    // 필수 조건: sessionId
                                    b.must(m -> m.term(t -> t.field("sessionId").value(sessionId)));
                                    // 필수 조건: timestamp 범위
                                    b.must(m -> m.range(r -> r.date(d -> d.field("timestamp")
                                            .from(durationStart)
                                            .to(durationEnd)
                                            .timeZone("Asia/Seoul")
                                    )));
                                    // 필수 조건: WITHDRAW
                                    b.must(m -> m.term(t -> t.field("transactionType").value("WITHDRAW")));

                                    // 선택 조건: userId
                                    if (userId != null) {
                                        b.must(m -> m.term(t -> t.field("userId").value(userId)));
                                    }
                                    return b;
                                })
                        )
                        .aggregations("category_count", a -> a
                                .terms(t -> t
                                        .field("category")
                                        .size(5)
                                        .order(NamedValue.of("total_amount", SortOrder.Desc))
                                )
                                .aggregations("total_amount", sa -> sa
                                        .sum(v -> v
                                                .script(sc -> sc.source("doc.containsKey('amount') ? doc['amount'].value : 0"))
                                        )
                                )
                        ),
                Void.class
        );

        return response.aggregations().get("category_count").sterms().buckets().array().stream()
                .map(b -> new ChartIncomeCountDto(
                        CategoryType.fromKey(b.key().stringValue()).getLabel(),
                        (long) b.aggregations().get("total_amount").sum().value(),
                        b.docCount()
                ))
                .collect(Collectors.toList());
    }


    public List<ChartIncomeIncomeExpenseDto> searchTransactionInfo(String sessionId, String durationStart, String durationEnd, String interval, String format, Integer userId) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String query = QueryBuilder.transactionInfoQuery(durationStart, durationEnd, sessionId, interval, format, userId);
        request.setJsonEntity(query);

        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);


        Map<String, ChartIncomeIncomeExpenseDto> resultsMap = StreamSupport.stream(
                root.path("aggregations").path("summary").path("buckets").spliterator(), false)
                .map(dateBucket -> {
                    String dateKey = dateBucket.path("key_as_string").asText();
                    JsonNode transactionBuckets = dateBucket.path("transaction_summary").path("buckets");

                    // 4. income 버킷에서 합계(total_amount) 추출
                    // path()는 노드가 없으면 MissingNode를 반환하므로 .asDouble(0.0)으로 기본값 처리
                    double incomeSum = transactionBuckets.path("income").path("total_amount").path("value").asDouble(0.0);

                    // 5. expense 버킷에서 합계(total_amount) 추출
                    double expenseSum = transactionBuckets.path("expense").path("total_amount").path("value").asDouble(0.0);

                    // 6. 추출한 정보로 ChartCategoryDto 객체 생성
                    return new ChartIncomeIncomeExpenseDto(
                            dateKey,
                            (long) incomeSum,
                            (long) expenseSum
                    );
                })
                // 7. 날짜(dateKey)를 키로, 생성된 DTO를 값으로 하는 Map으로 수집
                .collect(Collectors.toMap(ChartIncomeIncomeExpenseDto::name, dto -> dto));

        List<ChartIncomeIncomeExpenseDto> completeData = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(durationStart, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(durationEnd, DateTimeFormatter.ISO_LOCAL_DATE);

        LocalDate currentDate;
        // 시작 날짜를 각 interval의 시작점으로 정렬
        switch (interval) {
            case "1M":
                currentDate = startDate.withDayOfMonth(1);
                break;
            case "1W":
                currentDate = startDate.with(java.time.DayOfWeek.MONDAY);
                break;
            default: // daily
                currentDate = startDate;
                break;
        }

        while (!currentDate.isAfter(endDate)) {
            String dateKey = currentDate.format(DateTimeFormatter.ofPattern(format));
            ChartIncomeIncomeExpenseDto chartData = resultsMap.getOrDefault(dateKey, new ChartIncomeIncomeExpenseDto(dateKey, 0L, 0L));
            completeData.add(chartData);

            switch (interval) {
                case "1M":
                    currentDate = currentDate.plusMonths(1);
                    break;
                case "1W":
                    currentDate = currentDate.plusWeeks(1);
                    break;
                default: // daily
                    currentDate = currentDate.plusDays(1);
                    break;
            }
        }
        return completeData;
    }

    public List<ChartIncomeCountDto> searchCategoryByVomlumeTop5EachAgeGroup(String sessionId, List<Long> userIds, String durationStart, String durationEnd) throws IOException {
        List<FieldValue> userIdFieldValues = userIds.stream()
                .map(FieldValue::of)
                .collect(Collectors.toList());

        SearchResponse<Void> response = elasticsearchClient.search(s -> s
                        .index("transaction-logs")
                        .size(0)
                        .query(q -> q
                                .bool(b -> b
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
                                        .must(m -> m
                                                .term(t -> t
                                                        .field("sessionId")
                                                        .value(sessionId)
                                                )
                                        )
                                        .must(m -> m
                                                .terms(t -> t
                                                        .field("userId")
                                                        .terms(tv -> tv.value(userIdFieldValues))
                                                )
                                        )
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
                .map(b -> new ChartIncomeCountDto(
                        CategoryType.fromKey(b.key().stringValue()).getLabel(),
                        (long) b.aggregations().get("total_amount").sum().value(),
                        b.docCount()
                ))
                .collect(Collectors.toList());
    }

    private JsonNode executeAndGetBucketsForFinancial(String sessionId, Map<Integer, List<Long>> userIds, String durationStart, String durationEnd) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String query = QueryBuilder.incomeExpenseByAgeGroupQuery(sessionId, userIds, durationStart, durationEnd);
        request.setJsonEntity(query);

        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);

        return root.path("aggregations").path("age_group_summary").path("buckets");
    }

    public IncomeExpenseDto saerchIncomeExpense(String sessionId, String durationStart, String durationEnd, Integer userId) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String query = QueryBuilder.incomeExpenseQuery(sessionId, durationStart == null? "0000-01-01" : durationStart, durationEnd == null? "9999-12-31" : durationEnd, userId);
        request.setJsonEntity(query);

        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult).get("aggregations").get("financial_summary").get("buckets");

        long totalIncome = root.get("income").get("total_amount").get("value").longValue();
        long totalExpense = root.get("expense").get("total_amount").get("value").longValue();

        return new IncomeExpenseDto(totalIncome, totalExpense, totalIncome - totalExpense);
    }

    public MinMaxDayDto saerchMinMaxDay(String sessionId) throws IOException {
        SearchResponse<Void> minMaxResponse = elasticsearchClient.search(s -> s
                        .index("transaction-logs")
                        .size(0)
                        .query(q -> q.term(t -> t.field("sessionId").value(sessionId)))
                        .aggregations("min_date", a -> a.min(m -> m.field("timestamp")))
                        .aggregations("max_date", a -> a.max(m -> m.field("timestamp"))),
                Void.class
        );

        MinAggregate minAgg = minMaxResponse.aggregations().get("min_date").min();
        MaxAggregate maxAgg = minMaxResponse.aggregations().get("max_date").max();

        String durationStart = Instant.ofEpochMilli((long)minAgg.value()).atZone(ZoneId.of("Asia/Seoul")).toLocalDate().toString();
        String durationEnd = Instant.ofEpochMilli((long)maxAgg.value()).atZone(ZoneId.of("Asia/Seoul")).toLocalDate().toString();

        return new MinMaxDayDto(durationStart, durationEnd);
    }

    public CategoryAmountTransaction searchUserCategoryTradeAmount(String sessionId, LocalDate from, LocalDate to, Integer userId) throws IOException {
        Request request = new Request("GET", ES_END_POINT);
        String queryJson = QueryBuilder.userCategoryAmountQuery(sessionId, from, to, userId);
        request.setJsonEntity(queryJson);

        Response response = elasticsearchRestClient.performRequest(request);
        String jsonResult = EntityUtils.toString(response.getEntity());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResult);
        JsonNode buckets = root.path("aggregations").path("by_category").path("buckets");

        List<CategoryAmountTransaction.AmountTransactionSummary> summaries = new ArrayList<>();
        for (JsonNode bucket : buckets) {
            String category = bucket.path("key").asText();

            int amount;
            amount = bucket.path("total_amount").path("value").asInt(0);

            summaries.add(new CategoryAmountTransaction.AmountTransactionSummary(category, amount));
        }

        return new CategoryAmountTransaction(summaries);
    }
}
