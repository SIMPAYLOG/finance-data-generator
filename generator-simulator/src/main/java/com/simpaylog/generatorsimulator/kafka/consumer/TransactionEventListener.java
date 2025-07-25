package com.simpaylog.generatorsimulator.kafka.consumer;

import com.simpaylog.generatorcore.dto.TransactionRequestEvent;
import com.simpaylog.generatorsimulator.service.TransactionAsyncHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventListener {
    private final TransactionAsyncHandler transactionAsyncHandler;

    @KafkaListener(topics = "${spring.kafka.topic.tx.reqeust}")
    public void consumeTransactionRequest(TransactionRequestEvent event) {
        transactionAsyncHandler.handler(event);
    }

}
