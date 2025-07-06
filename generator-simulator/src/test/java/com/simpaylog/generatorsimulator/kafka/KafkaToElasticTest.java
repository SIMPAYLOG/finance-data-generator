package com.simpaylog.generatorsimulator.kafka;

import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.simpaylog.generatorsimulator.TestConfig;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",  // H2 임시 메모리 DB
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",  // Hibernate가 테이블 생성 안함
        "spring.sql.init.mode=never",          // SQL 스크립트(schema.sql 등) 실행 안함
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaToElasticTest extends TestConfig {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private final String bootstrapServers = "localhost:9092,localhost:9093,localhost:9094";

    static String indexedId;

    @Test
    @Order(1)
    @DisplayName("1. Kafka 토픽에 메시지 전송")
    public void testSendMessageToKafka() {
        String message = "{\"id\":\"1\", \"content\":\"hello kafka-elasticsearch\"}";
        kafkaTemplate.send("test-topic", message);
        kafkaTemplate.flush();
        Assertions.assertTrue(true, "Message sent to Kafka");
    }


    @Test
    @Order(2)
    @DisplayName("2. Kafka 메시지 직접 소비 후 Elasticsearch 저장")
    public void testConsumeAndIndexToElasticsearch() throws Exception {
        //Kafka Consumer 설정
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "test-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "false");

        //KafkaConsumer 생성 및 구독
        //test-topic은 KafkaTemplate.send(...)로 메세지를 보낸 상태여야함
        try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("test-topic"));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5)); //메세지 가져오기, 최대 5초 대기

            //elasticsearch에 메세지 저장
            for (ConsumerRecord<String, String> record : records) {
                Map<String, Object> doc = Map.of("message", record.value());
                IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                        .index("test-index")
                        .document(doc)
                );
                IndexResponse response = elasticsearchClient.index(request);
                indexedId = response.id();
            }

            Assertions.assertNotNull(indexedId, "Elasticsearch에 저장된 문서 ID가 있어야 함");
        }
    }
    @Test
    @Order(3)
    @DisplayName("3. Elasticsearch에 저장된 문서 삭제")
    public void testDeleteFromElasticsearch() throws Exception {
        Assertions.assertNotNull(indexedId, "삭제할 ID가 없습니다");

        // Elasticsearch 문서 삭제
        elasticsearchClient.delete(d -> d
                .index("test-index")
                .id(indexedId)
        );

        // 삭제 확인
        var response = elasticsearchClient.get(g -> g
                .index("test-index")
                .id(indexedId), Map.class);

        Assertions.assertFalse(response.found(), "Elasticsearch에서 문서가 삭제되지 않았습니다");
    }


    @Test
    @Order(4)
    @DisplayName("4. Kafka에서 토픽 삭제")
    public void testDeleteKafkaTopic() throws Exception {
        String topic = "test-topic";

        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient admin = AdminClient.create(config)) {
            // 삭제 요청
            admin.deleteTopics(Collections.singletonList(topic)).all().get();

            // 토픽 삭제 확인 (retry loop)
            boolean topicDeleted = false;
            for (int i = 0; i < 5; i++) {
                Thread.sleep(1000);
                var topics = admin.listTopics().names().get();
                if (!topics.contains(topic)) {
                    topicDeleted = true;
                    break;
                }
            }

            Assertions.assertTrue(topicDeleted, "Kafka 토픽이 삭제되지 않았습니다");
        }
    }
}
