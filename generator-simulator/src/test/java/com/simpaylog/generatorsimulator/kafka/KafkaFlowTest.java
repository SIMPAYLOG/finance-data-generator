package com.simpaylog.generatorsimulator.kafka;

import com.simpaylog.generatorsimulator.TestConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.springframework.test.util.AssertionErrors.assertTrue;

@Disabled
@EmbeddedKafka(partitions = 1, topics = { "test-topic" })
@AutoConfigureMockMvc
public class KafkaFlowTest extends TestConfig {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // 메시지를 수신하는 BlockingQueue
    private static final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    //Consumer가 Elasticsearch에 저장했다고 가정
    private void saveToElasticsearch(String message) {
        System.out.println("Elasticsearch 저장 로직 → " + message);
    }

    // Kafka Listener: 메시지를 수신하고 Elasticsearch 저장 요청
    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void consume(ConsumerRecord<String, String> record) {
        String message = record.value();
        messages.offer(message); // 테스트용 큐에 저장
        saveToElasticsearch(message); // 가상의 ES 저장 수행
    }

    @Test
    @DisplayName("1. Producer가 Kafka로 메시지를 발행하고, Consumer가 이를 수신 및 ES 저장 요청까지 수행하는지 테스트")
    public void testKafkaEndToEndFlow() throws InterruptedException {
        // given
        String testMessage = "Kafka 테스트 메시지";

        // when: Kafka에 메시지를 발행(Producer)
        kafkaTemplate.send("test-topic", testMessage);
        kafkaTemplate.flush();


        // then: 메시지가 Consumer를 통해 수신되었고, ES 저장 로직까지 수행됐는지 확인
        String received = messages.take(); // 최대 5초까지 기다릴 수도 있음
        System.out.println(testMessage);
        System.out.println(received);
        assertTrue("Kafka → Consumer로 메시지가 정확히 전달되어야 함", testMessage.equals(received));
    }
}

