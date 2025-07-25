package com.simpaylog.generatorsimulator.kafka.producer;

import com.simpaylog.generatorcore.dto.DailyTransactionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyTransactionResultProducer {

    private final KafkaTemplate<Integer, DailyTransactionResult> kafkaTemplate;

    @Value("${spring.kafka.topic.tx.response}")
    private String topic;

    public void send(DailyTransactionResult result) {
        kafkaTemplate.send(topic, result);
    }
}
