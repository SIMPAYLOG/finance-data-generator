package com.simpaylog.generatorsimulator.producer;

import com.simpaylog.generatorsimulator.dto.TransactionLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionLogProducer {

    private final KafkaTemplate<Integer, TransactionLog> kafkaTemplate;

    @Value("${spring.kafka.topic.transaction}")
    private String topic;

    public void send(TransactionLog transactionLog) {
        kafkaTemplate.send(topic, transactionLog);
    }
}
