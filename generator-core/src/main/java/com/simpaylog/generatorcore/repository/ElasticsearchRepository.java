package com.simpaylog.generatorcore.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.simpaylog.generatorcore.dto.Document.TransactionLogDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Repository
@RequiredArgsConstructor
public class ElasticsearchRepository {

    private final ElasticsearchClient client;

    public void findAllTransactionsForExport(Consumer<TransactionLogDocument> consumer) {
        List<FieldValue> searchAfter = null;

        try {
            while (true) {
                SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                        .index("transaction-logs")
                        .size(1000)
                        .sort(s -> s.field(f -> f.field("userId").order(SortOrder.Asc)))
                        .sort(s -> s.field(f -> f.field("timestamp").order(SortOrder.Asc)));

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
                    consumer.accept(hit.source());  // 호출 측에 바로 데이터 전달
                }

                // 마지막 문서의 정렬 기준 값을 다음 검색의 기준으로 사용
                searchAfter = hits.getLast().sort();
            }
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 데이터 조회 중 IOException 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch 데이터 조회 중 알 수 없는 오류 발생", e);
        }
    }
}