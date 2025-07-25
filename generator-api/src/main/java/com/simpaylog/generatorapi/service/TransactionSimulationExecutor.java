package com.simpaylog.generatorapi.service;

import com.simpaylog.generatorapi.kafka.producer.TransactionGenerationRequestProducer;
import com.simpaylog.generatorcore.dto.TransactionRequestEvent;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionSimulationExecutor {

    private final TransactionGenerationRequestProducer transactionGenerationRequestProducer;

    public void simulateTransaction(List<TransactionUserDto> users, LocalDate from, LocalDate to) {
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            for (TransactionUserDto user : users) {
                transactionGenerationRequestProducer.send(new TransactionRequestEvent(user, date));
            }
        }
    }
}
