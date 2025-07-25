package com.simpaylog.generatorapi.kafka.consumer;

import com.simpaylog.generatorapi.service.TransactionProgressTracker;
import com.simpaylog.generatorcore.dto.DailyTransactionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final TransactionProgressTracker transactionProgressTracker;

    @KafkaListener(topics = "${spring.kafka.topic.tx.response}")
    public void consumeTransactionResponse(DailyTransactionResult result) {
        transactionProgressTracker.increment("");
    }

}
