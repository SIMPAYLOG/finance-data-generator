package com.simpaylog.generatorapi.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {
    private final RestClient elasticsearchRestClient;
    private static final String ES_END_POINT = "/transaction-logs";


    @PostConstruct
    public void init() throws InterruptedException {
        int retry = 0;
        int maxRetry = 10;
        while (retry < maxRetry) {
            try {
                Response response = elasticsearchRestClient.performRequest(new Request("HEAD", ES_END_POINT));
                if (response.getStatusLine().getStatusCode() == 404) {
                    String query = getMappingQuery();
                    Request createRequest = new Request("PUT", ES_END_POINT);
                    createRequest.setJsonEntity(query);
                    elasticsearchRestClient.performRequest(createRequest);
                    log.warn("index: {} 매핑 안되어 있어 추가 완료", ES_END_POINT);
                }
                return;
            } catch (Exception e) {
                retry++;
                log.warn("ES 연결 재시도 {}/{}", retry, maxRetry);
                Thread.sleep(3000); // 3초 대기 후 재시도
            }
        }
        throw new IllegalStateException("Elasticsearch 연결 실패: 재시도 횟수 초과");
    }

    private String getMappingQuery() {
        return """
                {
                  "mappings": {
                    "properties": {
                      "timestamp":        { "type": "date" },
                      "amount":           { "type": "scaled_float", "scaling_factor": 100 },
                      "description":      {
                        "type": "text",
                        "fields": {
                          "keyword": { "type": "keyword" }
                        }
                      },
                      "userId":           { "type": "long" },
                      "uuid":             { "type": "keyword" },
                      "sessionId":        { "type": "keyword" },
                      "transactionType":  { "type": "keyword" },
                      "@timestamp":       { "type": "date" },
                      "@version":         { "type": "keyword" },
                      "category":         { "type": "keyword" },
                      "subcategory":      { "type": "keyword" }
                    }
                  }
                }
                """;
    }
}
