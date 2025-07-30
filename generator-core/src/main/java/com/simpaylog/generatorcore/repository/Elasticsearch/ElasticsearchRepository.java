package com.simpaylog.generatorcore.repository.Elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.simpaylog.generatorcore.dto.Document.TransactionLogDocument;
import com.simpaylog.generatorcore.exception.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Repository
@RequiredArgsConstructor
public class ElasticsearchRepository {

    private final ElasticsearchClient client;

    public void findAllTransactionsForExport(String sessionId, Consumer<TransactionLogDocument> consumer) {
        List<FieldValue> searchAfter = null;
        try {
            while (true) {
                SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                        .index("transaction-logs")
                        .size(1000)
                        .sort(s -> s.field(f -> f.field("userId").order(SortOrder.Asc)))
                        .sort(s -> s.field(f -> f.field("timestamp").order(SortOrder.Asc)))
                        .sort(s -> s.field(f -> f.field("uuid.keyword").order(SortOrder.Asc)))  // _id 추가
                        .query(q -> q.term(t -> t.field("sessionId.keyword").value(sessionId)));

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
            throw new CoreException("Elasticsearch 통신 중 IOException 발생");
        } catch (Exception e) {
            throw new CoreException("Elasticsearch 조회 중 알 수 없는 예외 발생");
        }
    }

}