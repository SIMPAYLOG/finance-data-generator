package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.TestConfig;
import com.simpaylog.generatorcore.enums.TransactionLogHeader;
import com.simpaylog.generatorcore.repository.redis.RedisRepository;
import com.simpaylog.generatorcore.service.UserService;
import com.simpaylog.generatorcore.dto.TransactionLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "spring.kafka.topic.transaction=topic-transaction",
        "spring.kafka.topic.tx.request=dummy-topic",
        "spring.kafka.topic.tx.response=dummy-topic",
        "spring.kafka.consumer.group-id=test-group"
})
@Import(TransactionExportService.class)
public class TransactionExportServiceTest extends TestConfig {
    @Autowired
    TransactionExportService transactionExportService;
    @MockitoBean
    UserService userService;
    @MockitoBean
    RedisRepository redisRepository;

    @Test
    public void ES에서_모든_데이터를_가져온다() {
        List<TransactionLog> results = transactionExportService.getAllTransaction();

        assertThat(results)
                .withFailMessage("Elasticsearch에서 조회된 결과가 null입니다.")
                .isNotNull();

        assertThat(results)
                .withFailMessage("Elasticsearch에서 결과를 가져왔지만 비어 있습니다.")
                .isNotEmpty();

        TransactionLog first = results.getFirst();
        System.out.println(first);

        assertThat(first.userId())
                .withFailMessage("첫 번째 데이터의 userId가 null입니다.")
                .isNotNull();

        assertThat(first.timestamp())
                .withFailMessage("첫 번째 데이터의 timestamp가 null입니다.")
                .isNotNull();

        System.out.println("가져온 데이터 수: " + results.size());
        System.out.println("첫 번째 데이터 userId: " + first.userId());
        System.out.println("첫 번째 데이터 timestamp: " + first.timestamp());
    }

    @Test
    public void JSON_형태로_정상_반환되는지_검증한다() {
        byte[] jsonBytes = transactionExportService.exportAllTransaction("json");
        String json = new String(jsonBytes, StandardCharsets.UTF_8);

        assertThat(json)
                .withFailMessage("JSON 직렬화 결과가 null이거나 비어 있습니다.")
                .isNotBlank()
                .contains("[", "{")  // 기본 JSON 구조 체크
                .satisfies(s -> {
                    for (var rc : TransactionLog.class.getRecordComponents()) {
                        String fieldName = rc.getName();
                        if (!s.contains("\"" + fieldName + "\"")) {
                            throw new AssertionError("JSON에 필드 '" + fieldName + "' 가 포함되어 있지 않습니다.");
                        }
                    }
                });

//    System.out.println("JSON 결과: " + json.substring(0, Math.min(500, json.length())) + "...");
    }

    @Test
    public void CSV_형태로_정상_반환되는지_검증한다() {
        byte[] csvBytes = transactionExportService.exportAllTransaction("csv");
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertThat(csv)
                .withFailMessage("CSV 직렬화 결과가 null이거나 비어 있습니다.")
                .isNotBlank()
                .satisfies(s -> { // 주요 키워드가 포함되어 있는지 확인
                    for (TransactionLogHeader header : TransactionLogHeader.values()) {
                        if (!s.contains(header.getDisplayName())) {
                            throw new AssertionError("CSV에 필드 '" + header.getDisplayName() + "' 가 포함되어 있지 않습니다.");
                        }
                    }
                })
                // CSV는 콤마로 구분된 형태인지 확인
                .contains(",");

//        System.out.println("CSV 결과:\n" + csv.lines().limit(5).reduce("", (a, b) -> a + "\n" + b));
    }
}
