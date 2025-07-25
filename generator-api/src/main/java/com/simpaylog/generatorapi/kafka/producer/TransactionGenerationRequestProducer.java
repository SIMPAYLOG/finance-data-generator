package com.simpaylog.generatorapi.kafka.producer;

import com.simpaylog.generatorcore.dto.TransactionRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionGenerationRequestProducer {
    private final KafkaTemplate<Integer, TransactionRequestEvent> kafkaTemplate;

    @Value("${spring.kafka.topic.tx.request}")
    private String topic;

    public void send(TransactionRequestEvent request) {
        kafkaTemplate.send(topic, request);
    }
}
