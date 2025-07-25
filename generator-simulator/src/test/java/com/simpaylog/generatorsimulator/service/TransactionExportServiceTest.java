package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.repository.redis.RedisRepository;
import com.simpaylog.generatorcore.service.UserService;
import com.simpaylog.generatorsimulator.dto.TransactionLog;
import com.simpaylog.generatorsimulator.kafka.producer.DailyTransactionResultProducer;
import com.simpaylog.generatorsimulator.kafka.producer.TransactionLogProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.topic.transaction=topic-transaction",
        "spring.kafka.topic.tx.request=dummy-topic",
        "spring.kafka.consumer.group-id=test-group"
})
@Import(TransactionExportService.class)
public class TransactionExportServiceTest {
    @Autowired
    TransactionExportService transactionExportService;
    @MockitoBean
    TransactionLogProducer transactionLogProducer;
    @MockitoBean
    DailyTransactionResultProducer dailyTransactionResultProducer;
    @MockitoBean
    TransactionGenerator transactionGenerator;
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

}
